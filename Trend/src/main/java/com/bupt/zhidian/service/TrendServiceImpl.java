package com.bupt.zhidian.service;

import com.bupt.zhidian.pojo.KalmanFilter;
import jama.Matrix;
import jkalman.JKalman;
import org.json.JSONArray;
import org.json.JSONObject;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TrendServiceImpl implements TrendService {
    @Override
    public String getPageOfTrend1(int market, String code) throws Exception {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        String url = "http://10.122.252.243:9090/" + "stock/dayKline/" + market + "/" + code;
        Page page = webClient.getPage(url);
        WebResponse response = page.getWebResponse();
        String jsonString = response.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);

        if(jsonObject.has("message")) {
            JSONObject errorObject = new JSONObject();
            errorObject.put("message", jsonObject.get("message"));
            errorObject.put("status", jsonObject.get("status"));
            return errorObject.toString();
        }

        JSONArray jsonArray = jsonObject.getJSONArray("klines");
        JSONObject[] klineObjects = new JSONObject[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            klineObjects[i] = jsonArray.getJSONObject(i);
        }

        klineObjects = oneMonth(klineObjects);

        List<Float> highestList = new ArrayList<>(klineObjects.length);
        List<Float> lowestList = new ArrayList<>(klineObjects.length);
        for (int i = 0; i < klineObjects.length; i++) {
            highestList.add(Float.parseFloat(klineObjects[i].getString("highest")));
            lowestList.add(Float.parseFloat(klineObjects[i].getString("lowest")));
        }

        highestList = kal(highestList);
        lowestList = kal(lowestList);

        List<Integer> min = new ArrayList<>(2);
        List<Integer> max = new ArrayList<>(2);
        int count1 = 0, count2 = 0;
        for (int i = klineObjects.length - 2; i > 0; i--) {
            if (highestList.get(i) > highestList.get(i + 1) &&
                    highestList.get(i) > highestList.get(i - 1) &&
                    count1 < 2) {
                max.add(i);
                count1++;
            }
            if (lowestList.get(i) > lowestList.get(i + 1) &&
                    lowestList.get(i) > lowestList.get(i - 1) &&
                    count2 < 2) {
                min.add(i);
                count2++;
            }
            if (count1 == 2 && count2 == 2) {
                break;
            }
        }
        while(count1 < 2) {
            max.add(0);
            count1++;
        }
        while(count2 < 2) {
            min.add(0);
            count2++;
        }

        JSONObject points = new JSONObject();
        points.put("min1", min.get(0));
        points.put("min2", min.get(1));
        points.put("max1", max.get(0));
        points.put("max2", max.get(1));

        JSONObject bigObject = new JSONObject().put("name", jsonObject.get("name"));
        bigObject.put("points", points);
        bigObject.put("priceOfMax", klineObjects[max.get(0)].get("highest"));
        bigObject.put("priceOfMin", klineObjects[min.get(0)].get("lowest"));

        if (Float.parseFloat(klineObjects[klineObjects.length - 1].getString("closing"))
                > Float.parseFloat(klineObjects[max.get(0)].getString("highest"))) {
            bigObject.put("trend", "upBreak");
        } else if (Float.parseFloat(klineObjects[klineObjects.length - 1].getString("closing"))
                < Float.parseFloat(klineObjects[max.get(0)].getString("lowest"))) {
            bigObject.put("trend", "downBreak");
        } else if ((Float.parseFloat(klineObjects[max.get(0)].getString("highest"))
                - Float.parseFloat(klineObjects[max.get(1)].getString("highest"))) > 0
                && (Float.parseFloat(klineObjects[min.get(0)].getString("lowest"))
                - Float.parseFloat(klineObjects[min.get(1)].getString("lowest"))) > 0) {
            bigObject.put("trend", "up");
        } else if ((Float.parseFloat(klineObjects[max.get(0)].getString("highest"))
                - Float.parseFloat(klineObjects[max.get(1)].getString("highest"))) < 0
                && (Float.parseFloat(klineObjects[min.get(0)].getString("lowest"))
                - Float.parseFloat(klineObjects[min.get(1)].getString("lowest"))) < 0) {
            bigObject.put("trend", "down");
        } else {
            bigObject.put("trend", "stable");
        }
        float increase = getPrice(market, code) - Float.parseFloat(klineObjects[min.get(0)].getString("lowest"))
                / Float.parseFloat(klineObjects[min.get(0)].getString("lowest"));
        float decrease = Float.parseFloat(klineObjects[max.get(0)].getString("highest")) - getPrice(market, code)
                / Float.parseFloat(klineObjects[max.get(0)].getString("highest"));
        bigObject.put("klineOfOne", Arrays.toString(klineObjects));
        bigObject.put("price", getPrice(market, code));
        bigObject.put("increase", increase);
        bigObject.put("decrease", decrease);
        bigObject.put("status", 0);

        return bigObject.toString();
    }

    @Override
    public String getPageOfTrend3(int market, String code) throws Exception {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        String url = "http://10.122.252.243:9090/" + "stock/weekKline/" + market + "/" + code;
        Page page = webClient.getPage(url);
        WebResponse response = page.getWebResponse();
        String jsonString = response.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);

        if(jsonObject.has("message")) {
            JSONObject errorObject = new JSONObject();
            errorObject.put("message", jsonObject.get("message"));
            errorObject.put("status", jsonObject.get("status"));
            return errorObject.toString();
        }

        JSONArray jsonArray = jsonObject.getJSONArray("klines");
        JSONObject[] klineObjects = new JSONObject[jsonArray.length()];
        for(int i = 0; i < jsonArray.length(); i++) {
            klineObjects[i] = jsonArray.getJSONObject(i);
        }

        klineObjects = threeMonths(klineObjects);

        List<Float> highestList = new ArrayList<>(klineObjects.length);
        List<Float> lowestList = new ArrayList<>(klineObjects.length);
        for(int i = 0; i < klineObjects.length; i++) {
            highestList.add(Float.parseFloat(klineObjects[i].getString("highest")));
            lowestList.add(Float.parseFloat(klineObjects[i].getString("lowest")));
        }

        highestList = kal(highestList);
        lowestList = kal(lowestList);

        List<Integer> min = new ArrayList<>(2);
        List<Integer> max = new ArrayList<>(2);
        int count1 = 0, count2 = 0;
        for(int i = klineObjects.length-2; i > 0; i--) {
            if(highestList.get(i) > highestList.get(i+1) &&
                    highestList.get(i) > highestList.get(i-1) &&
                    count1 < 2) {
                max.add(i);
                count1++;
            }
            if(lowestList.get(i) > lowestList.get(i+1) &&
                    lowestList.get(i) > lowestList.get(i-1) &&
                    count2 < 2) {
                min.add(i);
                count2++;
            }
            if(count1 == 2 && count2 == 2) {
                break;
            }
        }
        while(count1 < 2) {
            max.add(0);
            count1++;
        }
        while(count2 < 2) {
            min.add(0);
            count2++;
        }
        JSONObject points = new JSONObject();
        points.put("min1", min.get(0));
        points.put("min2", min.get(1));
        points.put("max1", max.get(0));
        points.put("max2", max.get(1));

        JSONObject bigObject = new JSONObject().put("name", jsonObject.get("name"));
        bigObject.put("points", points);
        bigObject.put("priceOfMax", klineObjects[max.get(0)].get("highest"));
        bigObject.put("priceOfMin", klineObjects[min.get(0)].get("lowest"));

        if(Float.parseFloat(klineObjects[klineObjects.length-1].getString("closing"))
                > Float.parseFloat(klineObjects[max.get(0)].getString("highest"))) {
            bigObject.put("trend", "upBreak");
        } else if(Float.parseFloat(klineObjects[klineObjects.length-1].getString("closing"))
                < Float.parseFloat(klineObjects[max.get(0)].getString("lowest"))) {
            bigObject.put("trend", "downBreak");
        } else if((Float.parseFloat(klineObjects[max.get(0)].getString("highest"))
                -Float.parseFloat(klineObjects[max.get(1)].getString("highest"))) > 0
                && (Float.parseFloat(klineObjects[min.get(0)].getString("lowest"))
                -Float.parseFloat(klineObjects[min.get(1)].getString("lowest"))) > 0) {
            bigObject.put("trend", "up");
        } else if((Float.parseFloat(klineObjects[max.get(0)].getString("highest"))
                -Float.parseFloat(klineObjects[max.get(1)].getString("highest"))) < 0
                && (Float.parseFloat(klineObjects[min.get(0)].getString("lowest"))
                -Float.parseFloat(klineObjects[min.get(1)].getString("lowest"))) < 0) {
            bigObject.put("trend", "down");
        } else {
            bigObject.put("trend", "down");
        }

        bigObject.put("klineOfOne", Arrays.toString(klineObjects));
        bigObject.put("price", getPrice(market, code));
        float increase = getPrice(market, code) - Float.parseFloat(klineObjects[min.get(0)].getString("lowest"))
                / Float.parseFloat(klineObjects[min.get(0)].getString("lowest"));
        float decrease = Float.parseFloat(klineObjects[max.get(0)].getString("highest")) - getPrice(market, code)
                / Float.parseFloat(klineObjects[max.get(0)].getString("highest"));
        bigObject.put("klineOfOne", Arrays.toString(klineObjects));
        bigObject.put("price", getPrice(market, code));
        bigObject.put("increase", increase);
        bigObject.put("decrease", decrease);
        bigObject.put("status", 0);

        return bigObject.toString();
    }

    private JSONObject[] threeMonths(JSONObject[] klineObjects) {
        JSONObject[] temp = new JSONObject[13];
        for(int i = 12; i >= 0; i--) {
            temp[i] = klineObjects[klineObjects.length-13+i];
        }
        return temp;
    }

    public JSONObject[] oneMonth(JSONObject[] jsonObjects) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(jsonObjects[jsonObjects.length-1].getString("time"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -1);
        date = calendar.getTime();
        String dateBefore = format.format(date);
        int index = 0;
        for(int i = 0; i < jsonObjects.length; i++) {
            if(dateBefore.equals(jsonObjects[i].getString("time")) || jsonObjects.length - i == 22) {
                index = i;
                break;
            }
        }
        JSONObject[] temp = new JSONObject[jsonObjects.length-index];
        for(int i = 0; i+index < jsonObjects.length; i++) {
            temp[i] = jsonObjects[i+index];
        }
        return temp;
    }

    public static List<Float> createFrom1D(List<Float> stream) throws Exception {
        final JKalman kalman = new JKalman(2, 1);
        // measurement [x]
        final Matrix m = new Matrix(1, 1);

        // transitions for x, dx
        double[][] tr = {{1, 0},
                {0, 1}};
        kalman.setTransition_matrix(new Matrix(tr));

        // 1s somewhere?
        kalman.setError_cov_post(kalman.getError_cov_post().identity());

        List<Float> floats = new ArrayList<>();
        stream.stream().forEach(value -> {
            m.set(0, 0, value);
            // state [x, dx]
            Matrix s = kalman.Predict();
            // corrected state [x, dx]
            Matrix c = kalman.Correct(m);
            floats.add((float) c.get(0, 0));
        });
        return floats;
    }

    @Override
    public Float getPrice(int market, String code) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        String url = "http://10.122.252.243:9090/" + "stock/trends/" + market + "/" + code;
        Page page = webClient.getPage(url);
        WebResponse response = page.getWebResponse();
        String jsonString = response.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray jsonArray = jsonObject.getJSONArray("price");
        return Float.parseFloat(jsonArray.getString(0));
    }

    @Override
    public String getPoints(int market, String code) throws IOException {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        String url = "http://10.122.252.243:9090/" + "stock/dayKline/" + market + "/" + code;
        Page page = webClient.getPage(url);
        WebResponse response = page.getWebResponse();
        String jsonString = response.getContentAsString();
        JSONObject jsonObject = new JSONObject(jsonString);

        if(jsonObject.has("message")) {
            JSONObject errorObject = new JSONObject();
            errorObject.put("message", jsonObject.get("message"));
            errorObject.put("status", jsonObject.get("status"));
            return errorObject.toString();
        }

        JSONArray jsonArray = jsonObject.getJSONArray("klines");
        JSONObject[] klineObjects = new JSONObject[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            klineObjects[i] = jsonArray.getJSONObject(i);
        }

        List<Float> highestList = new ArrayList<>(klineObjects.length);
        List<Float> lowestList = new ArrayList<>(klineObjects.length);
        for (int i = 0; i < klineObjects.length; i++) {
            highestList.add(Float.parseFloat(klineObjects[i].getString("highest")));
            lowestList.add(Float.parseFloat(klineObjects[i].getString("lowest")));
        }

        highestList = kal(highestList);
        lowestList = kal(lowestList);

        List<Integer> min = new ArrayList<>(2);
        List<Integer> max = new ArrayList<>(2);
        int count1 = 0, count2 = 0;
        for (int i = klineObjects.length - 2; i > 0; i--) {
            if (highestList.get(i) > highestList.get(i + 1) &&
                    highestList.get(i) > highestList.get(i - 1) &&
                    count1 < 2) {
                max.add(i);
                count1++;
            }
            if (lowestList.get(i) > lowestList.get(i + 1) &&
                    lowestList.get(i) > lowestList.get(i - 1) &&
                    count2 < 2) {
                min.add(i);
                count2++;
            }
            if (count1 == 2 && count2 == 2) {
                break;
            }
        }
        while(count1 < 2) {
            max.add(0);
            count1++;
        }
        while(count2 < 2) {
            min.add(0);
            count2++;
        }

        JSONObject points = new JSONObject();
        points.put("min1", min.get(0));
        points.put("min2", min.get(1));
        points.put("max1", max.get(0));
        points.put("max2", max.get(1));
        return points.toString();
    }

    public List<Float> kal(List<Float> list) {
        /*for(int i = 0; i < list.size(); i++) {
            list.set(i, list.get(i) / 100);
        }*/
        KalmanFilter kalmanfilter =new KalmanFilter();
        kalmanfilter.initial();

        float oldValue = list.get(0);
        ArrayList<Float> alist = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            float value = list.get(i);
            oldValue = kalmanfilter.KalmanFilter(oldValue,value);
            alist.add(oldValue);
        }

        /*for(int i = 0; i < alist.size(); i++) {
            alist.set(i, list.get(i) * 100);
        }*/
        System.out.println(list);
        System.out.println(alist);

        return alist;
    }
}
