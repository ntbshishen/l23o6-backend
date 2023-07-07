package org.fffd.l23o6.util.strategy.train;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Nullable;


public class KSeriesSeatStrategy extends TrainSeatStrategy {
    public static final KSeriesSeatStrategy INSTANCE = new KSeriesSeatStrategy();
     
    private final Map<Integer, String> SOFT_SLEEPER_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> HARD_SLEEPER_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> SOFT_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> HARD_SEAT_MAP = new HashMap<>();

    private final Map<KSeriesSeatType, Map<Integer, String>> TYPE_MAP = new HashMap<>() {{
        put(KSeriesSeatType.SOFT_SLEEPER_SEAT, SOFT_SLEEPER_SEAT_MAP);
        put(KSeriesSeatType.HARD_SLEEPER_SEAT, HARD_SLEEPER_SEAT_MAP);
        put(KSeriesSeatType.SOFT_SEAT, SOFT_SEAT_MAP);
        put(KSeriesSeatType.HARD_SEAT, HARD_SEAT_MAP);
    }};
    private final Map<KSeriesSeatType,Integer> ticket_price=new HashMap<>(){
        {
            put(KSeriesSeatType.HARD_SLEEPER_SEAT, 100);
            put(KSeriesSeatType.SOFT_SLEEPER_SEAT, 120);
            put(KSeriesSeatType.SOFT_SEAT,80 );
            put(KSeriesSeatType.HARD_SEAT,60 );
        }
    };

    private KSeriesSeatStrategy() {

        int counter = 0;

        for (String s : Arrays.asList("软卧1号上铺", "软卧2号下铺", "软卧3号上铺", "软卧4号上铺", "软卧5号上铺", "软卧6号下铺", "软卧7号上铺", "软卧8号上铺")) {
            SOFT_SLEEPER_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("硬卧1号上铺", "硬卧2号中铺", "硬卧3号下铺", "硬卧4号上铺", "硬卧5号中铺", "硬卧6号下铺", "硬卧7号上铺", "硬卧8号中铺", "硬卧9号下铺", "硬卧10号上铺", "硬卧11号中铺", "硬卧12号下铺")) {
            HARD_SLEEPER_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("1车1座", "1车2座", "1车3座", "1车4座", "1车5座", "1车6座", "1车7座", "1车8座", "2车1座", "2车2座", "2车3座", "2车4座", "2车5座", "2车6座", "2车7座", "2车8座")) {
            SOFT_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("3车1座", "3车2座", "3车3座", "3车4座", "3车5座", "3车6座", "3车7座", "3车8座", "3车9座", "3车10座", "4车1座", "4车2座", "4车3座", "4车4座", "4车5座", "4车6座", "4车7座", "4车8座", "4车9座", "4车10座")) {
            HARD_SEAT_MAP.put(counter++, s);
        }
    }

    public enum KSeriesSeatType implements SeatType {
        SOFT_SLEEPER_SEAT("软卧"), HARD_SLEEPER_SEAT("硬卧"), SOFT_SEAT("软座"), HARD_SEAT("硬座"), NO_SEAT("无座");
        private String text;
        KSeriesSeatType(String text){
            this.text=text;
        }
        public String getText() {
            return this.text;
        }
        public static KSeriesSeatType fromString(String text) {
            for (KSeriesSeatType b : KSeriesSeatType.values()) {
                if (b.text.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }


    public @Nullable String allocSeat(int startStationIndex, int endStationIndex, KSeriesSeatType type, boolean[][] seatMap) {
        int end=0;
        int start_location=0;
        String seat=null;
        switch (type) {
            case HARD_SLEEPER_SEAT -> start_location = 8;
            case SOFT_SEAT-> start_location = 20;
            case HARD_SEAT -> start_location= 35;
            default -> {
            }
        }
        // TODO
        for(int i=startStationIndex;i<=endStationIndex-1;i++){
            for(int j=start_location;j<start_location+TYPE_MAP.get(type).size();j++){
                if(!seatMap[i][j]){
                    seat=TYPE_MAP.get(type).get(j);
                    //System.out.println(seat);
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

    public Map<KSeriesSeatType, Integer> getLeftSeatCount(int startStationIndex, int endStationIndex, boolean[][] seatMap) {
        Map<KSeriesSeatStrategy.KSeriesSeatType,Integer> LeftTicket=new HashMap<>();
        int  SOFT_SLEEPER_SEAT_Count=0;
        int  HARD_SLEEPER_SEAT_Count=0;
        int  SOFT_SEAT_Count=0;
        int  HARD_SEAT_Count=0;
        for(int i=startStationIndex;i<=endStationIndex-1;i++){
            for(int j=0;j<seatMap[startStationIndex].length;j++){
                if(j<SOFT_SLEEPER_SEAT_MAP.size()){
                    if(!seatMap[i][j]){
                        SOFT_SLEEPER_SEAT_Count++;
                    }
                }
                else if(j<SOFT_SLEEPER_SEAT_MAP.size()+HARD_SLEEPER_SEAT_MAP.size()){
                    if(!seatMap[i][j]){
                        HARD_SLEEPER_SEAT_Count++;
                    }
                }
                else if(j<SOFT_SLEEPER_SEAT_MAP.size()+HARD_SLEEPER_SEAT_MAP.size()+SOFT_SEAT_MAP.size()){
                    if(!seatMap[i][j]){
                        SOFT_SEAT_Count++;
                    }
                }
                else{
                    if(!seatMap[i][j]){
                        HARD_SEAT_Count++;
                    }
                }
            }
        }
        LeftTicket.put(KSeriesSeatStrategy.KSeriesSeatType.SOFT_SLEEPER_SEAT,SOFT_SLEEPER_SEAT_Count);
        LeftTicket.put(KSeriesSeatStrategy.KSeriesSeatType.HARD_SLEEPER_SEAT,HARD_SLEEPER_SEAT_Count);
        LeftTicket.put(KSeriesSeatStrategy.KSeriesSeatType.SOFT_SEAT,SOFT_SEAT_Count);
        LeftTicket.put(KSeriesSeatStrategy.KSeriesSeatType.HARD_SEAT,HARD_SEAT_Count);
        return LeftTicket;
       // return null;
    }

    public boolean[][] initSeatMap(int stationCount) {
        return new boolean[stationCount - 1][SOFT_SLEEPER_SEAT_MAP.size() + HARD_SLEEPER_SEAT_MAP.size() + SOFT_SEAT_MAP.size() + HARD_SEAT_MAP.size()];
    }
    public int getTicketPrice(String type){
        if(ticket_price.get(KSeriesSeatType.fromString(type))!=null)
            return ticket_price.get(KSeriesSeatType.fromString(type));
        return 0;
    }
}
