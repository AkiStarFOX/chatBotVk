import ImgBot.ColorThief;
import Kmeans.Cluster;
import Kmeans.KMeans;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vk.api.sdk.callback.longpoll.responses.GetLongPollEventsResponse;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.List;
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


        KMeans kMeans = new KMeans("C:\\Users\\WoW-a\\IdeaProjects\\chatBotVk\\chatBot\\src\\main\\java\\Kmeans\\test.csv",3);
        for (Cluster c:kMeans.getPointsClusters()){
            System.out.println("TEST"+c);
        }
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
            if (!list.isEmpty()) {
                long time = System.currentTimeMillis();
                for (JsonObject js : list) {
                    JsonObject jsonObject = js.getAsJsonObject("object");
                    if (jsonObject.get("attachments") != null) {

                        JsonArray jsAttachment = jsonObject.getAsJsonArray("attachments");
                        Iterator phonesItr = jsAttachment.iterator();
                        JsonObject test = (JsonObject) phonesItr.next();
                        JsonObject jsPhoto = test.getAsJsonObject("photo");

                        for (int i = 1; i < 6; i++) {
                            ArrayList<Integer> listFromDB = getPhotoFromDBforImg(jsPhoto.get("photo_604").getAsString(), i);
                            List<String> attachIdList = loadPhotoInListForSend(listFromDB, actor, apiClient);

                            apiClient.messages()
                                    .send(actor)
                                    .message("Формула - " + i + "; Время запроса - " + ((float) (System.currentTimeMillis() - time) / 1000.f) + " sec")
                                    .userId(jsonObject.get("user_id").getAsInt())
                                    .randomId(random.nextInt())
                                    .attachment(attachIdList)
                                    .execute();
                        }
                    } else {
                        System.out.println("NOT HERE");
                    }

                    if (hexValid(jsonObject.get("body").getAsString())) {
                        ArrayList<Integer> listFromDB = getPhotoFromBD(jsonObject.get("body").getAsString().toLowerCase());
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

    private static List<String> loadPhotoInListForSend(ArrayList<Integer> listFromDB, GroupActor actor, VkApiClient apiClient) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < listFromDB.size(); i++) {
            File file = null;
            list.add("photo" + -104375368 + "_" + listFromDB.get(i));

        }
        return list;
    }

    private static ArrayList<Integer> getPhotoFromBD(String colorOfImg) {
        Connection connection = null;
        Set<Integer> list = new HashSet<>();
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
                        list.add(resultSet.getInt(2));
                        count++;
                        System.out.println(resultSet.getString(3));
                    } else {
                        break;
                    }
                }
                offset += 0.1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<Integer> list1 = new ArrayList<>(list);

        return list1;
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

    private static ArrayList<Integer> getPhotoFromDBforImg(String URL, int type) {

        Connection connection = null;
        Set<Integer> list = new LinkedHashSet();
        float limit = 1.6f;

        File file = null;
        BufferedImage img = null;
        try {
            String fileName = "google.png";
            img = ImageIO.read(new URL(URL));
            file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();

            }
            ImageIO.write(img, "png", file);

        } catch (Exception e) {
            e.printStackTrace();
        }


        PixelReader pixelReader = new PixelReader(img);


        try {
            connection = DriverManager.getConnection(MYSQL_URL, MYSQL_LOGIN, MYSQL_PASSWORD);
            Statement statement = connection.createStatement();
            while (list.size() < 10) {
//                System.out.println(sqlRequest(histo,limit));
//                ResultSet resultSet = statement.executeQuery(sqlRequest(histo,limit)) ;
                long time = System.currentTimeMillis();
                System.out.println(sqlPixelRequest(pixelReader, limit, type));
                ResultSet resultSet = statement.executeQuery(sqlPixelRequest(pixelReader, limit, type));
                System.out.println("time zaporsa = " + (float) (System.currentTimeMillis() - time) / 1000);
//


                while (resultSet.next()) {

                    list.add(resultSet.getInt(2));
                    System.out.println(resultSet.getString(3));

                }
                limit += 0.5;


            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        ArrayList<Integer> list1 = new ArrayList<>(list);

        return list1;
    }

    public static String sqlRequest(Histo histoOfImg, float limit) {
        StringBuilder s = new StringBuilder("select * from imageshsv where (");
        for (Map.Entry e : histoOfImg.getH().entrySet()) {
            s.append("pow((")
                    .append((float) e.getValue())
                    .append("-H")
                    .append(e.getKey())
                    .append("),2)+");
        }
        s.append("0)<" + limit + " and (");
        for (Map.Entry e : histoOfImg.getS().entrySet()) {
            s.append("pow((")
                    .append((float) e.getValue())
                    .append("-S")
                    .append(e.getKey())
                    .append("),2)+");
        }
        s.append("0)<" + limit + " and (");
        for (Map.Entry e : histoOfImg.getV().entrySet()) {
            s.append("pow((")
                    .append((float) e.getValue())
                    .append("-V")
                    .append(e.getKey())
                    .append("),2)+");
        }
        s.append("0)<" + limit);


        return s.toString();
    }

    public static String sqlPixelRequest(PixelReader pixelReader, float limit, int type) {
        Map<Integer, Map<Integer, Float>> resultMap = pixelReader.getResultMap();


        StringBuilder s = new StringBuilder("select * from imageshsv where (");

        s.append(mapStringRequest("R", resultMap.get(0), type));
        s.append(mapStringRequest("O", resultMap.get(1), type));
        s.append(mapStringRequest("Y", resultMap.get(2), type));
        s.append(mapStringRequest("LY", resultMap.get(3), type));
        s.append(mapStringRequest("LG", resultMap.get(4), type));
        s.append(mapStringRequest("G", resultMap.get(5), type));
        s.append(mapStringRequest("LB", resultMap.get(6), type));
        s.append(mapStringRequest("B", resultMap.get(7), type));
        s.append(mapStringRequest("DB", resultMap.get(8), type));
        s.append(mapStringRequest("P", resultMap.get(9), type));
        s.append(mapStringRequest("DP", resultMap.get(10), type));
        s.append(mapStringRequest("Pink", resultMap.get(11), type));
        s.append(0 + ")<" + limit + " and ");
        s.append("(Black >").append(pixelReader.getBlack()-0.1).append(" and Black < ").append(pixelReader.getBlack()+0.1).append(") and");
        s.append("(White >").append(pixelReader.getWhite()-0.1).append(" and White < ").append(pixelReader.getWhite()+0.1).append(") and");
        s.append("(Grey >").append(pixelReader.getGrey()-0.1).append(" and Grey < ").append(pixelReader.getGrey()+0.1).append(") and");
        s.append("(LG >").append(pixelReader.getLigth_grey()-0.1).append(" and LG < ").append(pixelReader.getLigth_grey()+0.1).append(")");


//        switch (type){
//            case 1:
//                s.append("sqrt(abs(").append(pixelReader.getBlack()).append("-Black))+");
//                s.append("sqrt(abs(").append(pixelReader.getGrey()).append("-Grey))+");
//                s.append("sqrt(abs(").append(pixelReader.getWhite()).append("-White))+");
//                s.append("sqrt(abs(").append(pixelReader.getLigth_grey()).append("-LG))");
//                break;
//            case 2:
//                s.append("sqrt(abs(sqrt(").append(pixelReader.getBlack()).append(")- sqrt(Black)))+");
//                s.append("sqrt(abs(sqrt(").append(pixelReader.getGrey()).append(") - sqrt(Grey)))+");
//                s.append("sqrt(abs(sqrt(").append(pixelReader.getWhite()).append(") - sqrt(White)))+");
//                s.append("sqrt(abs(sqrt(").append(pixelReader.getLigth_grey()).append(") - sqrt(LG)))");
//                break;
//            case 3:
//                s.append("sqrt(abs(pow(").append(pixelReader.getBlack()).append(","+1.f/4.f+")- pow(Black,"+1.f/4.f+")))+");
//                s.append("sqrt(abs(pow(").append(pixelReader.getGrey()).append(","+1.f/4.f+")- pow(Grey,"+1.f/4.f+")))+");
//                s.append("sqrt(abs(pow(").append(pixelReader.getWhite()).append(","+1.f/4.f+")- pow(White,"+1.f/4.f+")))+");
//                s.append("sqrt(abs(pow(").append(pixelReader.getLigth_grey()).append(","+1.f/4.f+")- pow(LG,"+1.f/4.f+")))");
//                break;
//            case 4:
//                s.append("sqrt(abs(pow(").append(pixelReader.getBlack()).append(","+1.f/8.f+")- pow(Black,"+1.f/4.f+")))+");
//                s.append("sqrt(abs(pow(").append(pixelReader.getGrey()).append(","+1.f/8.f+")- pow(Grey,"+1.f/8.f+")))+");
//                s.append("sqrt(abs(pow(").append(pixelReader.getWhite()).append(","+1.f/8.f+")- pow(White,"+1.f/8.f+")))+");
//                s.append("sqrt(abs(pow(").append(pixelReader.getLigth_grey()).append(","+1.f/8.f+")- pow(LG,"+1.f/8.f+")))");
//                break;
//            case 5:
//                s.append("sqrt(abs(pow(").append(pixelReader.getBlack()).append(","+1.f/16.f+")- pow(Black,"+1.f/16.f+")))+");
//                s.append("sqrt(abs(pow(").append(pixelReader.getGrey()).append(","+1.f/16.f+")- pow(Grey,"+1.f/16.f+")))+");
//                s.append("sqrt(abs(pow(").append(pixelReader.getWhite()).append(","+1.f/16.f+")- pow(White,"+1.f/16.f+")))+");
//                s.append("sqrt(abs(pow(").append(pixelReader.getLigth_grey()).append(","+1.f/16.f+")- pow(LG,"+1.f/16.f+")))");
//                break;
//
//        }
//        s.append("sqrt(abs(").append(pixelReader.getBlack()).append("-Black))+");
//        s.append("sqrt(abs(").append(pixelReader.getGrey()).append("-Grey))+");
//        s.append("sqrt(abs(").append(pixelReader.getWhite()).append("-White))+");
//        s.append("sqrt(abs(").append(pixelReader.getLigth_grey()).append("-LG))");

//        s.append(")<" + limit);
        System.out.println(limit);
        return s.toString();
    }

    public static String mapStringRequest(String name, Map<Integer, Float> map, int type) {
        StringBuilder s = new StringBuilder();
        switch (type) {

            case 1:
                for (Map.Entry m : map.entrySet()) {
                    s.append("sqrt(abs(")
                            .append(m.getValue())
                            .append("-" + name)
                            .append(m.getKey())
                            .append("))+");
                }
                break;
            case 2:
                for (Map.Entry m : map.entrySet()) {
                    s.append("sqrt(abs(")
                            .append("sqrt(" + m.getValue() + ")")
                            .append("-")
                            .append("sqrt(" + name + m.getKey() + ")")
                            .append("))+");
                }
                break;
            case 3:
                for (Map.Entry m : map.entrySet()) {
                    s.append("sqrt(abs(")
                            .append("pow(" + m.getValue() + "," + 1.f / 4.f + ")")
                            .append("-")
                            .append("pow(" + name + m.getKey() + "," + 1.f / 4.f + ")")
                            .append("))+");
                }
                break;
            case 4:
                for (Map.Entry m : map.entrySet()) {
                    s.append("sqrt(abs(")
                            .append("pow(" + m.getValue() + "," + 1.f / 8.f + ")")
                            .append("-")
                            .append("pow(" + name + m.getKey() + "," + 1.f / 8.f + ")")
                            .append("))+");
                }
                break;
            case 5:
                for (Map.Entry m : map.entrySet()) {
                    s.append("sqrt(abs(")
                            .append("pow(" + m.getValue() + "," + 1.f / 16.f + ")")
                            .append("-")
                            .append("pow(" + name + m.getKey() + "," + 1.f / 16.f + ")")
                            .append("))+");
                }
                break;
        }
        return s.toString();
    }
}