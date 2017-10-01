package onetwopunch.seoulinsangshot.com.seoulinsangshot.View.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import onetwopunch.seoulinsangshot.com.seoulinsangshot.Controller.Constants;
import onetwopunch.seoulinsangshot.com.seoulinsangshot.DataManager.Data.Weather1VO;
import onetwopunch.seoulinsangshot.com.seoulinsangshot.DataManager.Data.Weather2VO;
import onetwopunch.seoulinsangshot.com.seoulinsangshot.DataManager.Remote.RetrofitService;
import onetwopunch.seoulinsangshot.com.seoulinsangshot.Model.Model_Detail;
import onetwopunch.seoulinsangshot.com.seoulinsangshot.Model.Model_Weather1;
import onetwopunch.seoulinsangshot.com.seoulinsangshot.Model.Model_Weather2;
import onetwopunch.seoulinsangshot.com.seoulinsangshot.R;
import onetwopunch.seoulinsangshot.com.seoulinsangshot.View.TipActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.net.URLDecoder.decode;

/**
 * Created by kwakgee on 2017. 9. 17..
 */

public class Fragment_Detail_Info extends Fragment {
    FloatingActionButton fb;//지도보기 버튼
    FloatingActionButton fb2;//팁 다이얼로그 버튼
    TextView areaTv;//지역 텍스트뷰
    TextView smarthPhoneTv; //스마트폰 텍스트뷰
    TextView appInfoTv; //앱정보 텍스트뷰
    TextView transTv;   //교통수단 텍스트뷰
    TextView ddareungTv;    //따릉이 텍스트뷰
    TextView hitsTv;
    TextView likeTv;
    ImageView weather1;
    ImageView weather2;
    ImageView weather3;

    //디테일정보관련
    ArrayList<Model_Detail> detailMainList;
    String area;
    String initials;
    String name;
    String lat;
    String lng;
    String subway;
    String bus;
    String bicycle;
    String url;
    String smartPhone;
    String filter;
    String theme1;
    String theme2;
    String time;
    String tip;

    //시간관련
    String Date[];
    String baseDateArr[];
    String baseDate;
    String baseTimeArr[];
    String baseTime;

    //레트로핏 통신관련
    String weather_PTY;
    String weather_SKY;

    public Weather2VO repoList2;
    public Weather1VO repoList;
    public static ArrayList<Model_Weather1> weatherList1 = new ArrayList<Model_Weather1>();
    public static ArrayList<Model_Weather1> arr = new ArrayList<Model_Weather1>();

    public static ArrayList<Model_Weather2> weatherList2 = new ArrayList<Model_Weather2>();

    public Fragment_Detail_Info() {
    }

    public static Fragment_Detail_Info newInstance() {
        Fragment_Detail_Info fragment = new Fragment_Detail_Info();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_info, container, false);

        //텍스트뷰 연결
        smarthPhoneTv = (TextView) rootView.findViewById(R.id.smartphInfo);
        appInfoTv = (TextView) rootView.findViewById(R.id.appInfo);
        transTv = (TextView) rootView.findViewById(R.id.transportationInfo);
        ddareungTv = (TextView) rootView.findViewById(R.id.ddareungEInfo);
        areaTv = (TextView) rootView.findViewById(R.id.areaTextView);
        hitsTv=(TextView)rootView.findViewById(R.id.hitsTextView);
        likeTv=(TextView)rootView.findViewById(R.id.likeTextView);

        //날씨이미지 연결
        weather1 = (ImageView) rootView.findViewById(R.id.weather1);
        weather2 = (ImageView) rootView.findViewById(R.id.weather2);
        weather3 = (ImageView) rootView.findViewById(R.id.weather3);

        //플로팅버튼 연결
        fb = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fb2 = (FloatingActionButton) rootView.findViewById(R.id.fab2);



        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("geo:0,0?q="+lat+","+lng+"("+name+")");
                Intent it = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(it);
            }
        });
        fb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TipActivity.class);
                startActivity(intent);

            }
        });


        /////////////////////bundle 값 get하기///////////////
        area = getArguments().getString("area");
        initials = getArguments().getString("initials");
        name = getArguments().getString("name");
        lat = getArguments().getString("lat");
        lng = getArguments().getString("lng");
        subway = getArguments().getString("subway");
        bus = getArguments().getString("bus");
        bicycle = getArguments().getString("bicycle");
        url = getArguments().getString("url");
        smartPhone = getArguments().getString("smartPhone");
        filter = getArguments().getString("filter");
        theme1 = getArguments().getString("theme1");
        theme2 = getArguments().getString("theme2");
        time = getArguments().getString("time");
        tip = getArguments().getString("tip");
        ////////////////////////////////////////////////////

        //각 텍스트뷰에 정보 setText하기
        areaTv.setText(name);
        smarthPhoneTv.setText(smartPhone);
        appInfoTv.setText(filter);
        transTv.setText(subway+"\n"+bus);
        ddareungTv.setText(bicycle);
        //hitsTv.setText("Hits: "+viewCount);
        //likeTv.setText("Like: "+likeCount);

        //ServiceKey 디코딩 때문에 try,catch로 loadDataWeather1함수 호출
        try {
            loadDataWeather1();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return rootView;

    }

    ///////////////////////////날씨 API 호출////////////////////////////////
    public void loadDataWeather1() throws UnsupportedEncodingException {
        Retrofit client = new Retrofit.Builder().baseUrl(Constants.reqURL_Weather).addConverterFactory(GsonConverterFactory.create()).build();
        RetrofitService service = client.create(RetrofitService.class);

        //이거 굳이 안쓰고 바로 파라미터로 접근해도 됨.
        String nx = Constants.switchNX(area);
        String ny = Constants.switchNY(area);

        //현재시간 계산해서 파라미터로 넘겨줌. <<이 부분 메서드화 시키자>>
        long now = System.currentTimeMillis();
        java.util.Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

/*        //현재시간
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(date);
        String nowformat = sdfNow.format(nowCal.getTime());

        String nowDate [] = nowformat.split(" ");
        String nowTimeArr [] = nowDate[1].split(":");
        String nowTime = nowTimeArr[0] + nowTimeArr[1];*/

        //초단기용 시간 , 40분 전 시간을 보냄
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, -40);
        String formatDate = sdfNow.format(cal.getTime());

        Date = formatDate.split(" ");
        baseDateArr = Date[0].split("/");
        baseTimeArr = Date[1].split(":");
        baseDate = baseDateArr[0] + baseDateArr[1] + baseDateArr[2];
        baseTime = baseTimeArr[0] + baseTimeArr[1];

        //예보용 시간, 하루 전 20:00시를  보냄
        Calendar cal2= Calendar.getInstance();
        cal2.setTime(date);
        cal2.add(Calendar.DATE,-1);
        String formatDate2 = sdfNow.format(cal2.getTime());
        String Date2 [] = formatDate2.split(" ");
        String baseDateArr2 [] = Date2[0].split("/");
        String baseTimeArr2[] = Date2[1].split(":");
        String baseDate2 = baseDateArr2[0] + baseDateArr2[1] + baseDateArr2[2];
        String baseTime2 = "2000";

        //서비스키 디코딩
        String serviceKey = decode("V46Xl9KDTxcFmWFQKiAsxp9XNgTkiYEbdmyBArTL5%2BIeh73QsZ%2BhPB2PS9QEx7bOhzZcaDDyqLLg1hrlD5bKEw%3D%3D", "UTF-8");


        ///////////////예보용 Call 메서드 호출.//////////////////
        int tempBaseTime=Integer.parseInt(baseTime);
        Log.d("THISTIMEIS","TIME"+tempBaseTime);
        //현재시간이 1920(20:00에서 40분 뺀 값) 보다 크다면 (20:00시 보다 뒤라면)
        if((tempBaseTime)>1920)
        {
            //어제를 가리키던값을 오늘로, 시간을 08:00으로 바꿔서 통신.
            baseDate2=baseDate;
            baseTime2="0800";
        }
        Log.v("HERE_HERE",baseDate2);
        Log.v("HERE_HERE",baseTime2);
        Call<Weather2VO> call2=service.loadWeather2(serviceKey,nx,ny,baseDate2,baseTime2,"300","json");
        call2.enqueue(new Callback<Weather2VO>() {
            @Override
            public void onResponse(Call<Weather2VO> call, Response<Weather2VO> response) {
                if(response.isSuccessful())
                {
                    repoList2=response.body();
                    weatherList2=repoList2.getResponse().getBody().getItems().getItem();
                    Log.d("THISTIME","RESOPNSE"+response.raw());

                    long now = System.currentTimeMillis();
                    java.util.Date date = new Date(now);
                    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                    Calendar cal3= Calendar.getInstance();
                    cal3.setTime(date);
                    cal3.add(Calendar.DATE,1);
                    String formatDate3 = sdfNow.format(cal3.getTime());
                    String Date3 [] = formatDate3.split(" ");
                    String baseDateArr3 [] = Date3[0].split("/");
                    String baseDate3 = baseDateArr3[0] + baseDateArr3[1] + baseDateArr3[2];

                    Calendar cal4= Calendar.getInstance();
                    cal4.setTime(date);
                    cal4.add(Calendar.DATE,2);
                    String formatDate4 = sdfNow.format(cal4.getTime());
                    String Date4 [] = formatDate4.split(" ");
                    String baseDateArr4 [] = Date4[0].split("/");
                    String baseDate4 = baseDateArr4[0] + baseDateArr4[1] + baseDateArr4[2];

                    String toSKYvalue="";
                    String toPTYvalue="";
                    String toSKYvalue2="";
                    String toPTYvalue2="";

                    for(int i=0; i<weatherList2.size();i++) {
                        //내일이면서 00시 이면서 category==SKY라면.
                        if (weatherList2.get(i).getFcstDate() .equals(baseDate3) &&
                                weatherList2.get(i).getFcstTime().equals("0000") &&
                                weatherList2.get(i).getCategory().equals("SKY"))
                        {
                            toSKYvalue = weatherList2.get(i).getFcstValue();
                        }
                        //내일이면서 00시 이면서 catergory==PTY라면
                        else  if (weatherList2.get(i).getFcstDate().equals(baseDate3) &&
                                weatherList2.get(i).getFcstTime().equals("0000")&&
                                weatherList2.get(i).getCategory().equals("PTY"))
                        {
                            toPTYvalue=weatherList2.get(i).getFcstValue();
                        }
                        //모레이면서 00시 이면서 catergory==SKY라면
                        else if(weatherList2.get(i).getFcstDate().equals(baseDate4) &&
                                weatherList2.get(i).getFcstTime().equals("0000")&&
                                weatherList2.get(i).getCategory().equals("SKY"))
                        {
                            toSKYvalue2=weatherList2.get(i).getFcstValue();
                        }
                        //모레이면서 00시 이면서 catergory==PTY라면
                        else if(weatherList2.get(i).getFcstDate().equals(baseDate4) &&
                                weatherList2.get(i).getFcstTime().equals("0000")&&
                                weatherList2.get(i).getCategory().equals("PTY"))
                        {
                            toPTYvalue2=weatherList2.get(i).getFcstValue();
                        }
                    }

                    //내일 날씨

                    if (toPTYvalue.equals("1")) {
                        weather2.setImageResource(R.drawable.rain);
                    }
                    //강수가 '눈' or '진눈깨비' 이면
                    else if (toPTYvalue.equals("3") || toPTYvalue.equals("2")) {
                        weather2.setImageResource(R.drawable.snow);
                    }
                    //강수가 '비'도 '눈'도 '진눈깨비도 '아니고, 하늘상태가 '흐린상태' 라면 == 강수가 '없음' 이고 흐리다면
                    else if ((toPTYvalue.equals("0")) && (toSKYvalue.equals("2") || toSKYvalue.equals("3") || toSKYvalue.equals("4"))) {
                        weather2.setImageResource(R.drawable.cloudy);
                    } else if (toSKYvalue.equals("1")) {
                        weather2.setImageResource(R.drawable.sunny);
                    } else {
                        weather2.setImageResource(R.drawable.nomal);
                    }

                    //모레 날씨

                    if (toPTYvalue2.equals("1")) {
                        weather3.setImageResource(R.drawable.rain);
                    }
                    //강수가 '눈' or '진눈깨비' 이면
                    else if (toPTYvalue2.equals("3") || toPTYvalue2.equals("2")) {
                        weather3.setImageResource(R.drawable.snow);
                    }
                    //강수가 '비'도 '눈'도 '진눈깨비도 '아니고, 하늘상태가 '흐린상태' 라면 == 강수가 '없음' 이고 흐리다면
                    else if ((toPTYvalue2.equals("0")) && (toSKYvalue2.equals("2") || toSKYvalue2.equals("3") || toSKYvalue2.equals("4"))) {
                        weather3.setImageResource(R.drawable.cloudy);
                    } else if (toSKYvalue2.equals("1")) {
                        weather3.setImageResource(R.drawable.sunny);
                    } else {
                        weather3.setImageResource(R.drawable.nomal);
                    }


                }
            }

            @Override
            public void onFailure(Call<Weather2VO> call, Throwable t) {
                Log.v("brokenError","error");
            }
        });

        ///////////////////////실시간 초단기실황 날씨호출///////////////////////////////////

        Call<Weather1VO> call = service.loadWeather1(serviceKey, nx, ny, baseDate, baseTime, "json");
        call.enqueue(new Callback<Weather1VO>() {
            @Override
            public void onResponse(Call<Weather1VO> call, Response<Weather1VO> response) {
                if (response.isSuccessful()) {
                    repoList = response.body();
                    Log.d("HI", "ResponsedURL" + response.raw());
                    weatherList1 = repoList.getResponse().getBody().getItems().getItem();

                    for (int i = 0; i < weatherList1.size(); i++) {
                        String baseDate = weatherList1.get(i).getBaseDate();

                        String baseTime = weatherList1.get(i).getBaseTime();
                        String category = weatherList1.get(i).getCategory();
                        String nx = weatherList1.get(i).getNx();
                        String ny = weatherList1.get(i).getNy();
                        String obsrValue = weatherList1.get(i).getObsrValue();
                        if (category.equals("PTY")) {
                            arr.add(0, new Model_Weather1(baseDate, baseTime, category, nx, ny, obsrValue));
                        }
                        if (category.equals("SKY")) {
                            arr.add(1, new Model_Weather1(baseDate, baseTime, category, nx, ny, obsrValue));
                        }
                    }
                    switch (arr.get(0).getObsrValue())  //강수형태
                    {
                        case "0":
                            weather_PTY = "nomal"; //없음
                            break;
                        case "1":
                            weather_PTY = "rain"; //비
                            break;
                        case "2":
                            weather_PTY = "rain_snow"; //비+눈=진눈깨비
                            break;
                        case "3":
                            weather_PTY = "snow"; //눈
                    }
                    switch (arr.get(1).getObsrValue())  //하늘상태
                    {
                        case "1":
                            weather_SKY = "sunny"; //맑음
                            break;
                        case "2":
                            weather_SKY = "little_cloudy"; //구름조금
                            break;
                        case "3":
                            weather_SKY = "cloudy"; //구름많음
                            break;
                        case "4":
                            weather_SKY = "gray"; //흐림
                    }

                    //강수가 '비' 이면
                    if (weather_PTY.equals("rain")) {
                        weather1.setImageResource(R.drawable.rain);
                    }
                    //강수가 '눈' or '진눈깨비' 이면
                    else if (weather_PTY.equals("snow") || weather_PTY.equals("rain_snow")) {
                        weather1.setImageResource(R.drawable.snow);
                    }
                    //강수가 '비'도 '눈'도 '진눈깨비도 '아니고, 하늘상태가 '흐린상태' 라면 == 강수가 '없음' 이고 흐리다면
                    else if ((weather_PTY.equals("nomal")) && (weather_SKY.equals("lttle_cloudy") || weather_SKY.equals("cloudy") || weather_SKY.equals("gray"))) {
                        weather1.setImageResource(R.drawable.cloudy);
                    } else if (weather_SKY.equals("sunny")) {
                        weather1.setImageResource(R.drawable.sunny);
                    } else {
                        weather1.setImageResource(R.drawable.nomal);
                    }

                } else {
                    int statusCode = response.code();
                }
            }

            @Override
            public void onFailure(Call<Weather1VO> call, Throwable t) {
                Log.d("MainActivity", "error loading from API");
            }
        });
    }
    ////////////////////////////////////////////////////////////////////////
}
