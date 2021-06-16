

import java.util.*;

public class Main2 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int[] in = new int[6];
        for (int i = 0; i < 6; i++) {
            in[i] = input.nextInt();
        }
        int[][] other = new int[in[5]][2];
        for (int i = 0; i < in[5]; i++) {
            other[i][0]=input.nextInt()-1;
            other[i][1]=input.nextInt()-1;
        }

        int opts;
        opts = input.nextInt();

        TreeSet<Integer> optSet = new TreeSet<>();
        Map<Integer,List<int[]>> time2MoveMap = new HashMap<>();
        int[][] operation = new int[opts][4];

        for (int i = 0; i < opts; i++) {
            operation[i][0] = input.nextInt()-1;
            operation[i][1] = input.nextInt();
            operation[i][2] = input.nextInt();
            String s = input.next();
            if("W".equals(s))operation[i][3]=0;
            else if ("A".equals(s))operation[i][3]=1;
            else if ("S".equals(s))operation[i][3]=2;
            else if ("D".equals(s))operation[i][3]=3;
            optSet.add(operation[i][1]);

            if (time2MoveMap.get(operation[i][1])==null){
                List<int[]> l = new ArrayList<>();
                l.add(operation[i]);
                time2MoveMap.put(operation[i][1],l);

            }else {
                List<int[]> l = time2MoveMap.get(operation[i][1]);
                l.add(operation[i]);
                time2MoveMap.put(operation[i][1],l);
            }
        }
        int check = input.nextInt();
        TreeSet<Integer> checkTime = new TreeSet<>();
        int[] watchTime = new int[check];
        for (int i = 0; i < check; i++) {
            int c = input.nextInt();
            checkTime.add(c);
            watchTime[i]= c;
        }
        Mno mno = new Mno();
        mno.mnoSolution(in[0],in[1],in[2]-1,in[3]-1,in[4],other,optSet,time2MoveMap,checkTime,watchTime );




    }
}
class Mno{
    public void mnoSolution(double M, double N, double X, double Y, double R, int[][] other, TreeSet<Integer> optSet,
                            Map<Integer, List<int[]>> time2MoveMap,TreeSet<Integer>checkTimeSet,int[] watchTime){
        double edge = 2*R+1;
        double x1 = Math.max(X=edge/2,0.0);
        double x2 = X+edge/2;
        double y1 = Math.max(Y-edge/2,0.0);
        double y2 = Y+edge/2 ;
        Map<Integer,Integer> resMap = new HashMap<>();

        int start = 0;
        while(!checkTimeSet.isEmpty()){
            if(!optSet.isEmpty()&&start==optSet.first()){
                List<int[]> playerMovesList = time2MoveMap.get(start);
                for (int[] player:playerMovesList){
                    move((int)M,(int)N,player[0],other,player);
                }
                optSet.pollFirst();
            }
            if(!checkTimeSet.isEmpty()&&start==checkTimeSet.first()){
                int cnt = peoples(x1,x2,y1,y2,other);
                resMap.put(start,cnt);
                checkTimeSet.pollFirst();
            }
            start++;
        }
        for (int i = 0; i < watchTime.length-1; i++) {
            System.out.print(resMap.get(watchTime[i])+" ");
        }
        System.out.println(resMap.get(watchTime[watchTime.length-1]));

    }
    int[][] direc = {{0,1},{-1,0},{0,-1},{1,0}};
    public void move(int w,int h,int player,int[][] other,int[] moves){
        other[player][0]+=(direc[moves[3]][0]*moves[2]);
        other[player][1]+=(direc[moves[3]][1]*moves[2]);
    }
    public int peoples(double x1,double x2,double y1,double y2,int[][]other){
        int cnt = 0;
        for (int[] p:other){
            if(p[0]>=x1 && p[0]<=x2 && p[1]>=y1 && p[1]<=y2){
                cnt++;
            }

        }
        return cnt;
    }

}

