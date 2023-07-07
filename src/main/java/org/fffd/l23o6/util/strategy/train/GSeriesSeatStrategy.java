package org.fffd.l23o6.util.strategy.train;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Nullable;


public class GSeriesSeatStrategy extends TrainSeatStrategy {
    public static final GSeriesSeatStrategy INSTANCE = new GSeriesSeatStrategy();
     
    private final Map<Integer, String> BUSINESS_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> FIRST_CLASS_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> SECOND_CLASS_SEAT_MAP = new HashMap<>();

    private final Map<GSeriesSeatType, Map<Integer, String>> TYPE_MAP = new HashMap<>() {{
        put(GSeriesSeatType.BUSINESS_SEAT, BUSINESS_SEAT_MAP);
        put(GSeriesSeatType.FIRST_CLASS_SEAT, FIRST_CLASS_SEAT_MAP);
        put(GSeriesSeatType.SECOND_CLASS_SEAT, SECOND_CLASS_SEAT_MAP);
    }};
    private final Map<GSeriesSeatType,Integer> ticket_price=new HashMap<>(){
        {
            put(GSeriesSeatType.BUSINESS_SEAT, 200);
            put(GSeriesSeatType.FIRST_CLASS_SEAT, 150);
            put(GSeriesSeatType.SECOND_CLASS_SEAT, 100);
        }
    };


    private GSeriesSeatStrategy() {

        int counter = 0;

        for (String s : Arrays.asList("1车1A","1车1C","1车1F")) {
            BUSINESS_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("2车1A","2车1C","2车1D","2车1F","2车2A","2车2C","2车2D","2车2F","3车1A","3车1C","3车1D","3车1F")) {
            FIRST_CLASS_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("4车1A","4车1B","4车1C","4车1D","4车1F","4车2A","4车2B","4车2C","4车2D","4车2F","4车3A","4车3B","4车3C","4车3D","4车3F")) {
            SECOND_CLASS_SEAT_MAP.put(counter++, s);
        }
        
    }


    public enum GSeriesSeatType implements SeatType {
        BUSINESS_SEAT("商务座"), FIRST_CLASS_SEAT("一等座"), SECOND_CLASS_SEAT("二等座"), NO_SEAT("无座");
        private String text;
        GSeriesSeatType(String text){
            this.text=text;
        }
        public String getText() {
            return this.text;
        }
        public static GSeriesSeatType fromString(String text) {
            for (GSeriesSeatType b : GSeriesSeatType.values()) {
                if (b.text.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }
   // public Long getTicketMoney(String type){
        //if(GSeriesSeatType.valueOf(type)==GSeriesSeatType.BUSINESS_SEAT){

       // }
    //}
    public  boolean[][] resetseat(String seat,int start,int end,boolean[][] seatMap){
        int seat_location=0;
        if(BUSINESS_SEAT_MAP.containsValue(seat)){
            for(Integer key: BUSINESS_SEAT_MAP.keySet()){
                if(BUSINESS_SEAT_MAP.get(key).equals(seat)){
                    seat_location=key;
                }
            }

        }
        if(FIRST_CLASS_SEAT_MAP.containsValue(seat)){
            for(Integer key: FIRST_CLASS_SEAT_MAP.keySet()){
                if(FIRST_CLASS_SEAT_MAP.get(key).equals(seat)){
                    seat_location=key;
                }
            }
        }
        if(SECOND_CLASS_SEAT_MAP.containsValue(seat)) {
            for(Integer key: SECOND_CLASS_SEAT_MAP.keySet()){
                if(SECOND_CLASS_SEAT_MAP.get(key).equals(seat)){
                    seat_location=key;
                }
            }
        }
        System.out.println(seat_location);
        for(int i=start;i<=end-1;i++){
            if(seatMap[i][seat_location]){
                System.out.println(seat_location);
                seatMap[i][seat_location]=false;
               // System.out.println(seatMap[i][seat_location]);
                break;
            }
        }
            return seatMap;
    }

    public @Nullable String allocSeat(int startStationIndex, int endStationIndex, GSeriesSeatType type, boolean[][] seatMap) {
        int end=0;
        int start_location=0;
        String seat=null;
        switch (type) {
            case FIRST_CLASS_SEAT -> start_location = 3;
            case SECOND_CLASS_SEAT -> start_location = 15;
            default -> {
            }
        }
        // TODO
        for(int i=startStationIndex;i<=endStationIndex-1;i++){
            for(int j=start_location;j<start_location+TYPE_MAP.get(type).size();j++){
                if(!seatMap[i][j]){
                    seat=TYPE_MAP.get(type).get(j);
                    System.out.println(seat);
                    seatMap[i][j]=true;
                    end=1;
                    break;
                }
            }
            if(end==1){
                break;
            }
        }
        return seat;
    }

    public Map<GSeriesSeatType, Integer> getLeftSeatCount(int startStationIndex, int endStationIndex, boolean[][] seatMap) {
        // TODO
       Map<GSeriesSeatType,Integer> LeftTicket=new HashMap<>();
        int  BUSINESS_SEAT_Count=0;
        int  FIRST_CLASS_SEAT_Count=0;
        int  SECOND_CLASS_SEAT_Count=0;
        for(int i=startStationIndex;i<=endStationIndex-1;i++){
            for(int j=0;j<seatMap[startStationIndex].length;j++){
                if(j<BUSINESS_SEAT_MAP.size()){
                    if(!seatMap[i][j]){
                        BUSINESS_SEAT_Count++;
                    }
                }
                else if(j<FIRST_CLASS_SEAT_MAP.size()+BUSINESS_SEAT_MAP.size()){
                    if(!seatMap[i][j]){
                        FIRST_CLASS_SEAT_Count++;
                    }
                }
                else{
                    if(!seatMap[i][j]){
                        SECOND_CLASS_SEAT_Count++;
                    }
                }
            }
        }
        LeftTicket.put(GSeriesSeatType.BUSINESS_SEAT,BUSINESS_SEAT_Count);
        LeftTicket.put(GSeriesSeatType.FIRST_CLASS_SEAT,FIRST_CLASS_SEAT_Count);
        LeftTicket.put(GSeriesSeatType.SECOND_CLASS_SEAT,SECOND_CLASS_SEAT_Count);
        return LeftTicket;
    }

    public boolean[][] initSeatMap(int stationCount) {
        return new boolean[stationCount - 1][BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size() + SECOND_CLASS_SEAT_MAP.size()];
    }
    public int getTicketPrice(String type){
        if(ticket_price.get(GSeriesSeatType.fromString(type))!=null)
            return ticket_price.get(GSeriesSeatType.fromString(type));
        return 0;
    }
}
