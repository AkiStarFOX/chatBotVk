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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class Main {

    private final static String PROPERTIES_FILE = "config.properties";
    private final static Random random = new Random();


    public static void main(String[] args) throws Exception {
        Properties properties = readProperties();

        HttpTransportClient client = new HttpTransportClient();
        VkApiClient apiClient = new VkApiClient(client);

        GroupActor actor = initVkApi(apiClient, readProperties());

//
//while (true) {
//    Thread.sleep(1000);
//    GetResponse getResponse=apiClient.messages().get(actor).count(10).execute();
//    List<Message> messageArrayList = getResponse.getItems();
//    for (Message m : messageArrayList) {
//        if (!m.isReadState()) {
//            apiClient.messages().send(actor).message("DONT SLEEP ON WORK!").userId(m.getUserId()).randomId(random.nextInt()).execute();
//
//        }
//    }
//}
        File file = null;
        try {
            String fileName = "google.png";
            BufferedImage img = ImageIO.read(new URL("http://webiconspng.com/wp-content/uploads/2017/09/Google-PNG-Image-21405.png"));
            file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();

            }
            ImageIO.write(img, "png", file);

        } catch (Exception e) {
            e.printStackTrace();
        }

//
        PhotoUpload photoUpload = apiClient.photos().getMessagesUploadServer(actor).execute();
        MessageUploadResponse messageUploadResponse = apiClient.upload().photoMessage(photoUpload.getUploadUrl(), file).execute();


        List<Photo> photoList = apiClient.photos().saveMessagesPhoto(actor, messageUploadResponse.getPhoto())
                .server(messageUploadResponse.getServer())
                .hash(messageUploadResponse.getHash())
                .execute();
        Photo photo = photoList.get(0);
        String attachId = "photo" + photo.getOwnerId() + "_" + photo.getId();
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
                apiClient.messages()
                        .send(actor)
                        .message("work")
                        .userId(jsonObject.get("user_id").getAsInt())
                        .randomId(random.nextInt())
                        .attachment(attachId)
                        .execute();

            }

            ts = getLongPollServerResponse.getTs();
        }
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
}