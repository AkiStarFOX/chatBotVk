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


            for (JsonObject js : list) {
                JsonObject jsonObject = js.getAsJsonObject("object");
                System.out.println(jsonObject.get("body"));
                ArrayList<String> listFromDB = getPhotoFromBD(jsonObject.get("body").getAsString().toLowerCase());
                List<String> attachIdList = loadPhotoInListForSend(listFromDB, actor, apiClient);
                for (String s : listFromDB) {
                    System.out.println("ListFromDB" + s);
                }
                for (String s : attachIdList) {
                    System.out.println("ListVK" + s);
                }
                if (hexValid(jsonObject.get("body").getAsString())){
                    if(attachIdList.size()==0){
                        apiClient.messages()
                                .send(actor)
                                .message("Нет таких картинок=(")
                                .userId(jsonObject.get("user_id").getAsInt())
                                .randomId(random.nextInt())
                                .execute();
                    }else{
                        apiClient.messages()
                                .send(actor)
                                .message("work")
                                .userId(jsonObject.get("user_id").getAsInt())
                                .randomId(random.nextInt())
                                .attachment(attachIdList)
                                .execute();
                    }
                }else {
                    apiClient.messages()
                            .send(actor)
                            .message("Цвет введен неправильно, попробуйте еще раз")
                            .userId(jsonObject.get("user_id").getAsInt())
                            .randomId(random.nextInt())
                            .execute();
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
        try {
            connection = DriverManager.getConnection(MYSQL_URL, MYSQL_LOGIN, MYSQL_PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from images where color ='" + colorOfImg + "'");
            int count = 0;
            while (resultSet.next()) {
                System.out.println("+1 picha");
                if (count < 15) {
                    list.add(resultSet.getString(2));
                    count++;
                } else {
                    break;
                }
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

    private static Boolean hexValid(String hex){
         String HEX_PATTERN
                = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";

         Pattern pattern;
         Matcher matcher;


         pattern = Pattern.compile(HEX_PATTERN);



            matcher = pattern.matcher(hex);
            return matcher.matches();

    }
}