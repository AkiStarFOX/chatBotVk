import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vk.api.sdk.callback.longpoll.responses.GetLongPollEventsResponse;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.base.Image;
import com.vk.api.sdk.objects.groups.responses.GetLongPollServerResponse;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.responses.GetResponse;
import com.vk.api.sdk.objects.photos.Photo;
import com.vk.api.sdk.objects.photos.PhotoUpload;
import com.vk.api.sdk.objects.photos.responses.MessageUploadResponse;
import com.vk.api.sdk.queries.messages.MessagesSendQuery;
import com.vk.api.sdk.queries.upload.UploadPhotoMessageQuery;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private final static String PROPERTIES_FILE = "config.properties";
    private final static Random random = new Random();
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/imgdb?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static final String MYSQL_LOGIN = "root";
    private static final String MYSQL_PASSWORD = "root";


    public static void main(String[] args) throws Exception {
        Properties properties = readProperties();
        HttpTransportClient client = new HttpTransportClient();
        VkApiClient apiClient = new VkApiClient(client);
        GroupActor actor = initVkApi(apiClient, readProperties());


        com.vk.api.sdk.objects.groups.responses.GetLongPollServerResponse getResponse = apiClient.groups().getLongPollServer(actor).execute();

        int ts = getResponse.getTs();

        while (true) {
            GetLongPollEventsResponse getLongPollServerResponse;
            try {
                getLongPollServerResponse = apiClient.longPoll().getEvents(getResponse.getServer(), getResponse.getKey(), ts).waitTime(25).execute();

            } catch (Exception e) {
                getLongPollServerResponse = apiClient.longPoll().getEvents(getResponse.getServer(), getResponse.getKey(), ts).waitTime(25).execute();

            }

            List<JsonObject> list = getLongPollServerResponse.getUpdates();
            System.out.println(list.isEmpty());
            if(!list.isEmpty()) {

                for (JsonObject js : list) {
                    JsonObject jsonObject = js.getAsJsonObject("object");


                    if (hexValid(jsonObject.get("body").getAsString())) {
                        ArrayList<String> listFromDB = getPhotoFromBD(jsonObject.get("body").getAsString().toLowerCase());
                        List<String> attachIdList = loadPhotoInListForSend(listFromDB, actor, apiClient);
                        if (attachIdList.size() == 0) {
                            apiClient.messages()
                                    .send(actor)
                                    .message("Нет таких картинок=(")
                                    .userId(jsonObject.get("user_id").getAsInt())
                                    .randomId(random.nextInt())
                                    .execute();
                        } else {
                            apiClient.messages()
                                    .send(actor)
                                    .message("work")
                                    .userId(jsonObject.get("user_id").getAsInt())
                                    .randomId(random.nextInt())
                                    .attachment(attachIdList)
                                    .execute();
                        }
                    } else {
                        apiClient.messages()
                                .send(actor)
                                .message("Цвет введен неправильно, попробуйте еще раз")
                                .userId(jsonObject.get("user_id").getAsInt())
                                .randomId(random.nextInt())
                                .execute();
                    }
                }
            }

            ts = getLongPollServerResponse.getTs();
        }
    }

    private static List<String> loadPhotoInListForSend(ArrayList<String> listFromDB, GroupActor actor, VkApiClient apiClient) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < listFromDB.size(); i++) {
            File file = null;
            try {
                String fileName = "google" + i + ".png";
                BufferedImage img = ImageIO.read(new URL(listFromDB.get(i)));
                file = new File(fileName);
                if (!file.exists()) {
                    file.createNewFile();

                }
                ImageIO.write(img, "png", file);

            } catch (Exception e) {
                e.printStackTrace();
            }


            List<Photo> photoList = null;
            try {
                PhotoUpload photoUpload = apiClient.photos().getMessagesUploadServer(actor).execute();
                MessageUploadResponse messageUploadResponse = apiClient.upload().photoMessage(photoUpload.getUploadUrl(), file).execute();
                photoList = apiClient.photos().saveMessagesPhoto(actor, messageUploadResponse.getPhoto())
                        .server(messageUploadResponse.getServer())
                        .hash(messageUploadResponse.getHash())
                        .execute();
                
            } catch (ApiException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            }


            Photo photo = photoList.get(0);

            list.add("photo" + photo.getOwnerId() + "_" + photo.getId());

        }
        return list;
    }

    private static ArrayList<String> getPhotoFromBD(String colorOfImg) {
        Connection connection = null;
        ArrayList<String> list = new ArrayList<>();
        Color defaultColorRGB = Color.decode(colorOfImg);
        HSV hsv = new HSV(defaultColorRGB.getRed(), defaultColorRGB.getGreen(), defaultColorRGB.getBlue());
        float offset = 0.f;
        float startH1;
        float startS1;
        float startV1;
        float endH1;
        float endS1;
        float endV1;

        try {
            connection = DriverManager.getConnection(MYSQL_URL, MYSQL_LOGIN, MYSQL_PASSWORD);
            Statement statement = connection.createStatement();
            while (list.size() < 10) {

                startH1 = hsv.getH() - offset / 2;
                if (startH1 < 0.00f) startH1 = 0.f;
                endH1 = hsv.getH() + offset / 2;
                if (endH1 > 1.0f) endH1 = 1.0f;

                startS1 = hsv.getS() - offset / 2;
                if (startS1 < 0.00f) startS1 = 0.f;
                endS1 = hsv.getS() + offset / 2;
                if (endS1 > 1.0f) endS1 = 1.0f;

                startV1 = hsv.getV() - offset / 2;
                if (startV1 < 0.00f) startV1 = 0.f;
                endV1 = hsv.getV() + offset / 2;
                if (endV1 > 1.0f) endV1 = 1.0f;


                ResultSet resultSet = statement.executeQuery("select * from imagesHSV where " +
                        "(H1 >='" + startH1 + "'and H1<='" + endH1 + "'" +
                        "and S1 >='" + startS1 + "'and S1<='" + endS1 + "'" +
                        "and V1 >='" + startV1 + "'and V1<='" + endV1 + "') " +
                        "or" +
                        "(H2 >='" + startH1 + "'and H2<='" + endH1 + "'" +
                        "and S2 >='" + startS1 + "'and S2<='" + endS1 + "'" +
                        "and V2 >='" + startV1 + "'and V2<='" + endV1 + "') " +
                        "or" +
                        "(H3 >='" + startH1 + "'and H3<='" + endH1 + "'" +
                        "and S3 >='" + startS1 + "'and S3<='" + endS1 + "'" +
                        "and V3 >='" + startV1 + "'and V3<='" + endV1 + "') " +
                        "or" +
                        "(H4 >='" + startH1 + "'and H4<='" + endH1 + "'" +
                        "and S4 >='" + startS1 + "'and S4<='" + endS1 + "'" +
                        "and V4 >='" + startV1 + "'and V4<='" + endV1 + "') " +
                        "or" +
                        "(H5 >='" + startH1 + "'and H5<='" + endH1 + "'" +
                        "and S5 >='" + startS1 + "'and S5<='" + endS1 + "'" +
                        "and V5 >='" + startV1 + "'and V5<='" + endV1 + "')");
                int count = 0;
                while (resultSet.next()) {

                    if (count < 10) {
                        list.add(resultSet.getString(2));
                        count++;
                    } else {
                        break;
                    }
                }
                System.out.println(offset);
                offset += 0.1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        return list;
    }

    private static GroupActor initVkApi(VkApiClient apiClient, Properties properties) {
        int groupId = Integer.parseInt(properties.getProperty("groupId"));
        String token = properties.getProperty("token");
        if (groupId == 0 || token == null) throw new RuntimeException("Params are not set");
        GroupActor actor = new GroupActor(groupId, token);


        return actor;
    }

    private static Properties readProperties() throws FileNotFoundException {
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
        if (inputStream == null)
            throw new FileNotFoundException("property file '" + PROPERTIES_FILE + "' not found in the classpath");

        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException("Incorrect properties file");
        }
    }

    private static Boolean hexValid(String hex) {
        String HEX_PATTERN
                = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";

        Pattern pattern;
        Matcher matcher;


        pattern = Pattern.compile(HEX_PATTERN);


        matcher = pattern.matcher(hex);
        return matcher.matches();

    }
}