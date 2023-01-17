## 2023-01-16
- 요구사항 명세서 작성
- 사용 기술스택 명세서 작성
- 재무제표 API 데이터 가져와서 필터링하고 추출하는 코드 작성 ( 연결재무제표,재무제표 어떤 데이터가 맞는지 확인 필요, 분기별 보고서 제한 생각해둬야함 (요청건수 제한이 1만건 언저리인데 요청건수가 10만건 가까이 되기 떄문))
```java
package com.example.test;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.example.DTO.DartCorp;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Main {
    public static final String SERVICE_KEY = "587afc333f77de19b08c7971d93cc48706b90822";
    public static ArrayList<DartCorp> dartlist = new ArrayList<>();
    public static String[] year = new String[]{"2015","2016","2017","2018","2019","2020","2021","2022"};
    public static String[] repocode = new String[]{"11011","11013","11012","11014"};
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        //getDartList();
        //dartlist.forEach(o -> System.out.println("법인명 : " + o.getCorpName() + "\n" + "고유 코드 : " + o.getCorpCode() + "\n" + "종목코드 : " + o.getStock_code() + "\n" ));
        //System.out.println("총 " + dartlist.size());
        StringBuilder urlBuilder = new StringBuilder("https://opendart.fss.or.kr/api/fnlttSinglAcnt.json");
        urlBuilder.append("?" + URLEncoder.encode("crtfc_key", "UTF-8") + "=" + SERVICE_KEY); /* Service Key */
        urlBuilder.append("&" + URLEncoder.encode("corp_code", "UTF-8") + "=" + URLEncoder.encode("00126380", "UTF-8")); /* 페이지번호 */
        urlBuilder.append("&" + URLEncoder.encode("bsns_year", "UTF-8") + "=" + URLEncoder.encode("2022", "UTF-8")); /* 한 페이지 결과 수 */
        urlBuilder.append("&" + URLEncoder.encode("reprt_code", "UTF-8") + "=" + URLEncoder.encode("11014", "UTF-8"));
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        if(conn.getResponseCode() != 200){
            System.out.println("에러발생");
        }
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            System.out.println(line);
        }
    }


    public static void getDartList() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse("C:\\CORPCODE.xml");

        doc.getDocumentElement().normalize();

        Element root = doc.getDocumentElement();

        NodeList n_list = root.getElementsByTagName("list");
        Element el;

        for (int i = 0; i < n_list.getLength(); i++) {
            el = (Element) n_list.item(i);
            if (!getTagValue("stock_code", el).equals(" ")) {
                dartlist.add(DartCorp.builder()
                        .corpCode(getTagValue("corp_code", el))
                        .corpName(getTagValue("corp_name", el))
                        .stock_code(getTagValue("stock_code", el))
                        .modifyDate(getTagValue("modify_date", el))
                        .build());
            }

        }

    }

    private static String getTagValue(String tag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        if (nValue == null)
            return null;
        return nValue.getNodeValue();
    }
}

```

---
### 2023-01-17
- 기능 명세서 작성
- 코딩 컨벤션 회의

java stream을 이용한 제무재표 종목 필터링, XML 파일 파싱 , json 데이터 파싱 으로 원하는 데이터 추출하는 코드 작성
```java
package com.example.test;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.DTO.Corporation;
import com.example.DTO.DartCorp;
//import org.json.JSONArray;
//import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Main {
    public static final String SERVICE_KEY = "587afc333f77de19b08c7971d93cc48706b90822";
    public static final String SERVICE_KEY2 = "87fABOEIcdWtsvwjsYXjoVbKfO7Oms4Jap9J8psfwew3kVvfO5hmZo8TSM9qqBS49RD%2BV8S3rukAg3J9M%2FE%2Blg%3D%3D";
    public static ArrayList<DartCorp> dartlist = new ArrayList<>();
    private static ArrayList<Corporation> corp = new ArrayList<>();
    public static String[] year = new String[]{"2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022"};
    public static String[] repocode = new String[]{"11011", "11013", "11012", "11014"};

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        getCorpList();
        getDartList();
        int k = 1;

//        dartlist.forEach(o -> System.out.println( "법인명 : " + o.getCorpName() + "\n" + "고유 코드 : " + o.getCorpCode() + "\n" + "종목코드 : " + o.getStock_code() + "\n" ));
        System.out.println("총 " + dartlist.size());
        System.out.println("총 " + corp.size());

        List<DartCorp> newdart = dartlist.stream()
                .filter(dart -> corp.stream().anyMatch(c -> dart.getStock_code().equals(c.getSrtnCd())))
                .collect(Collectors.toList());
        newdart.forEach(n -> System.out.println(n.getCorpName() + " " + n.getStock_code()));
        System.out.println(newdart.size());
//        for (int i = 0; i < dartlist.size(); i++){
//            System.out.println(corp.get(2280).getSrtnCd());
//            if ( corp.get(2280).getSrtnCd().equals(dartlist.get(i).getStock_code())){
//                System.out.println(dartlist.get(i).getCorpName() + " " + i);
//                break;
//            }
//        }
        String cCode = "";
        for (int i = 0; i < year.length; i++) {
            for (int j = 0; j < 1; j++) {
                System.out.println(dartlist.get(i).getCorpCode());
                StringBuilder urlBuilder = new StringBuilder("https://opendart.fss.or.kr/api/fnlttSinglAcnt.json");
                urlBuilder.append("?" + URLEncoder.encode("crtfc_key", "UTF-8") + "=" + SERVICE_KEY); /* Service Key */
                urlBuilder.append("&" + URLEncoder.encode("corp_code", "UTF-8") + "=" + URLEncoder.encode("00126380", "UTF-8")); /* 페이지번호 */
                urlBuilder.append("&" + URLEncoder.encode("bsns_year", "UTF-8") + "=" + URLEncoder.encode(year[i], "UTF-8")); /* 한 페이지 결과 수 */
                urlBuilder.append("&" + URLEncoder.encode("reprt_code", "UTF-8") + "=" + URLEncoder.encode("11011", "UTF-8"));
                System.out.println(urlBuilder.toString());
                URL url = new URL(urlBuilder.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");

                if (conn.getResponseCode() != 200) {
                    System.out.println("에러발생");
                }
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    System.out.println(line);
                    JSONObject item = new JSONObject(line);
                    if (item.get("status").toString().equals("000")){
                        JSONArray list = item.getJSONArray("list");
                        for (int b= 0 ; b < list.length() ; b++){
                            if ( (!list.getJSONObject(b).get("fs_nm").equals("연결재무제표")) &&(list.getJSONObject(b).get("account_nm").toString().equals("매출액") || list.getJSONObject(b).get("account_nm").toString().equals("당기순이익") || list.getJSONObject(b).get("account_nm").toString().equals("영업이익"))){
                                System.out.println(list.getJSONObject(b).get("account_nm")+ "  " + list.getJSONObject(b).get("thstrm_amount"));
                            }
                        }
                    }else{
                        System.out.println("해당년도의 데이터가 존재하지 않습니다");
                    };
                }
            }
        }
    }


    public static void getDartList() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse("C:\\CORPCODE.xml");

        doc.getDocumentElement().normalize();

        Element root = doc.getDocumentElement();

        NodeList n_list = root.getElementsByTagName("list");
        Element el;

        for (int i = 0; i < n_list.getLength(); i++) {
            el = (Element) n_list.item(i);
            if (!getTagValue("stock_code", el).equals(" ")) {
                dartlist.add(DartCorp.builder()
                        .corpCode(getTagValue("corp_code", el))
                        .corpName(getTagValue("corp_name", el))
                        .stock_code(getTagValue("stock_code", el))
                        .modifyDate(getTagValue("modify_date", el))
                        .build());
            }

        }

    }

    private static String getTagValue(String tag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        if (nValue == null)
            return null;
        return nValue.getNodeValue();
    }

    private static void getCorpList() throws IOException {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1160100/service/GetKrxListedInfoService/getItemInfo");
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + SERVICE_KEY2); /* Service Key */
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /* 페이지번호 */
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=1000000"); /* 한 페이지 결과 수 */
        urlBuilder.append("&" + URLEncoder.encode("resultType", "UTF-8") + "=json");
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        if (conn.getResponseCode() != 200) {
            System.out.println("에러발생");
        }
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;

        JSONArray item = null;
        while ((line = rd.readLine()) != null) {
            JSONObject json = new JSONObject(line);
            JSONObject res = json.getJSONObject("response");
            JSONObject body = res.getJSONObject("body");
            JSONObject items = body.getJSONObject("items");
            item = items.getJSONArray("item");
        }

        for (int i = 0; i < item.length(); i++) {
//            System.out.println(i + "  번쨰");
//           System.out.println("회사명 : " + item.getJSONObject(i).get("corpNm"));
//            System.out.println("종목 코드 : " +  item.getJSONObject(i).get("srtnCd").toString().substring(1) );
            //if(item.getJSONObject(i).get("basDt").equals("20230116")) {
            corp.add(new Corporation.Builder()
                    .setBasDt(item.getJSONObject(i).get("basDt").toString())
                    .setSrtnCd(item.getJSONObject(i).get("srtnCd").toString().substring(1))
                    .setIsinCd(item.getJSONObject(i).get("isinCd").toString())
                    .setMrktCtg(item.getJSONObject(i).get("mrktCtg").toString())
                    .setItmsNm(item.getJSONObject(i).get("itmsNm").toString())
                    .setCrno(item.getJSONObject(i).get("crno").toString())
                    .setCorpNm(item.getJSONObject(i).get("corpNm").toString())
                    .build()
            );
//            }else{
//                System.out.println("에러 발생");
//                System.out.println(i);
//                System.out.println("회사명 : " + item.getJSONObject(i).get("corpNm"));
//                System.out.println(item.getJSONObject(i).get("basDt"));
//                System.out.println(item.getJSONObject(i).get("srtnCd"));
//                System.out.println(item.getJSONObject(i).get("isinCd"));
//                System.out.println(item.getJSONObject(i).get("mrktCtg"));
//                System.out.println(item.getJSONObject(i).get("itmsNm"));
//           }
        }
    }

}
```
스켈레톤코드. DB 설계와 같이 파싱할 데이터 확정하고 완성할 예정
