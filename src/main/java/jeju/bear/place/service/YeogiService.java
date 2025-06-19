package jeju.bear.place.service;

import com.google.gson.*;
import jeju.bear.place.dto.PlaceDto;
import jeju.bear.place.dto.RoomDto;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class YeogiService {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(java.time.Duration.ofSeconds(15))
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .readTimeout(java.time.Duration.ofSeconds(10))
            .writeTimeout(java.time.Duration.ofSeconds(10))
            .build();

    private static final Gson gson = new Gson();
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36";

    public List<PlaceDto> fetchPlaceIds(String keyword, String checkIn, String checkOut, int personal) throws Exception {
        List<PlaceDto> result = new ArrayList<>();
        int page = 1;

        while (true) {
            String qs = String.format("keyword=%s&checkIn=%s&checkOut=%s&personal=%d&freeForm=false&page=%d",
                    URLEncoder.encode(keyword, StandardCharsets.UTF_8), checkIn, checkOut, personal, page);

            String html = safeGet("https://www.yeogi.com/domestic-accommodations?" + qs);
            String buildId = extractBuildId(html);
            if (buildId == null) break;

            String jsonUrl = String.format("https://www.yeogi.com/_next/data/%s/domestic-accommodations.json?%s", buildId, qs);
            String json = safeGet(jsonUrl);

            JsonObject props = gson.fromJson(json, JsonObject.class)
                    .getAsJsonObject("pageProps");
            JsonElement accommodations = props.get("accommodationsData");

            JsonArray list = accommodations.isJsonArray()
                    ? accommodations.getAsJsonArray()
                    : accommodations.getAsJsonObject().getAsJsonArray("contents");

            if (list == null || list.isEmpty()) break;

            for (JsonElement el : list) {
                JsonObject meta = el.getAsJsonObject().getAsJsonObject("meta");
                if (meta != null && meta.has("id") && meta.has("name")) {

                    Double latitude = null;
                    Double longitude = null;

                    if (meta.has("location")) {
                        JsonObject location = meta.getAsJsonObject("location");
                        if (location.has("latitude")) {
                            latitude = location.get("latitude").getAsDouble();
                        }
                        if (location.has("longitude")) {
                            longitude = location.get("longitude").getAsDouble();
                        }
                    }


                    // 이미지 URL 추출
                    String imageUrl = null;
                    if (meta.has("images") && meta.get("images").isJsonArray()) {
                        JsonArray images = meta.getAsJsonArray("images");
                        if (images.size() > 0) {
                            imageUrl = images.get(0).getAsString(); // 대표 이미지 한 장만 사용
                        }
                    }

                    // PlaceDto 생성 및 결과 추가
                    PlaceDto place = new PlaceDto();
                    place.setId(meta.get("id").getAsString());
                    place.setName(meta.get("name").getAsString());
                    place.setLatitude(latitude);
                    place.setLongitude(longitude);
                    place.setImageUrl(imageUrl);

                    result.add(place);
                }
            }

            page++;
            Thread.sleep(200); // 예의상 요청 딜레이
        }

        return result;
    }


    public List<RoomDto> fetchRoomsWithPrices(String placeId, String checkIn, String checkOut, int personal) throws Exception {
        List<RoomDto> rooms = new ArrayList<>();
        String qs = String.format("checkIn=%s&checkOut=%s&personal=%d&freeForm=false", checkIn, checkOut, personal);

        String html = safeGet(String.format("https://www.yeogi.com/domestic-accommodations/%s?%s", placeId, qs));
        String buildId = extractBuildId(html);
        if (buildId == null) throw new RuntimeException("buildId를 찾을 수 없습니다.");

        String jsonUrl = String.format("https://www.yeogi.com/_next/data/%s/domestic-accommodations/%s.json?%s", buildId, placeId, qs);
        String json = safeGet(jsonUrl);

        JsonObject props = gson.fromJson(json, JsonObject.class)
                .getAsJsonObject("pageProps");
        JsonObject accInfo = props.getAsJsonObject("accommodationInfo");

        JsonArray roomArr = accInfo.getAsJsonArray("rooms");

        for (JsonElement el : roomArr) {
            JsonObject room = el.getAsJsonObject();
            String roomId = room.has("roomId") ? room.get("roomId").getAsString() : room.get("id").getAsString();
            String roomName = room.has("roomName") ? room.get("roomName").getAsString() : room.get("name").getAsString();

            Long finalPrice = 0L;
            List<String> imageUrls = new ArrayList<>();

            if (room.has("stay")) {
                JsonObject stay = room.getAsJsonObject("stay");
                if (stay.has("price")) {
                    JsonObject priceObj = stay.getAsJsonObject("price");

                    if (priceObj.has("discountTotalPrice") && !priceObj.get("discountTotalPrice").isJsonNull()) {
                        finalPrice = priceObj.get("discountTotalPrice").getAsLong();
                    } else if (priceObj.has("totalPrice") && !priceObj.get("totalPrice").isJsonNull()) {
                        finalPrice = priceObj.get("totalPrice").getAsLong();
                    } else if (priceObj.has("discountPrice") && !priceObj.get("discountPrice").isJsonNull()) {
                        finalPrice = priceObj.get("discountPrice").getAsLong();
                    } else if (priceObj.has("salePrice") && !priceObj.get("salePrice").isJsonNull()) {
                        finalPrice = priceObj.get("salePrice").getAsLong();
                    }
                }
            }

            // ✅ images 안의 객체들의 image 필드에서 추출
            if (room.has("images") && room.get("images").isJsonArray()) {
                JsonArray images = room.getAsJsonArray("images");
                for (JsonElement imgEl : images) {
                    JsonObject imgObj = imgEl.getAsJsonObject();
                    if (imgObj.has("image")) {
                        imageUrls.add(imgObj.get("image").getAsString());
                    }
                }
            }

//            // ✅ newImages도 마찬가지로 처리
//            if (room.has("newImages") && room.get("newImages").isJsonArray()) {
//                JsonArray newImages = room.getAsJsonArray("newImages");
//                for (JsonElement imgEl : newImages) {
//                    JsonObject imgObj = imgEl.getAsJsonObject();
//                    if (imgObj.has("image")) {
//                        imageUrls.add(imgObj.get("image").getAsString());
//                    }
//                }
//            }

            RoomDto roomDto = new RoomDto();
            roomDto.setRoomId(roomId);
            roomDto.setRoomName(roomName);
            roomDto.setPrice(finalPrice);
            roomDto.setImages(imageUrls);
            rooms.add(roomDto);
        }

        return rooms;
    }



    private String safeGet(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return Objects.requireNonNull(response.body()).string();
        }
    }

    private String extractBuildId(String html) {
        Matcher matcher = Pattern.compile("\\\"buildId\\\":\\\"([^\\\"]+)\\\"").matcher(html);
        return matcher.find() ? matcher.group(1) : null;
    }
}