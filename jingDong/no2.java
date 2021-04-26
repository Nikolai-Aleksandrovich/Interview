package com.company;

import java.util.*;

public class Main2 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int number = input.nextInt();
        number--;
        HashSet<Integer> set = new HashSet<>();
        for (int i = 1; i <= number; i++) {
            set.add(i);
        }
        for (int i = 1; i <= number ; i++) {
            set.remove(i);
        }



    }
    private void Dac(int number,HashSet<Integer> set,int start){
        int length = 0;
        boolean isJump = false;
        while(!set.isEmpty()&& !isJump){
            for (Integer ele :
                    set) {
                if (start+1 == ele){
                    set.remove(start+1);
                    start=start+1;
                    length++;
                }else if (start+2==ele){
                    set.remove(start+2);
                    start = start+2;
                    length++;
                }else if (start-1==ele){
                    set.remove(start-1);
                    start = start-1;
                    length++;
                }else if(start-2==ele){
                    set.remove(start-2);
                    start = start-2;
                    length++;
                }else {
                    isJump=true;
                    break;

                }
            }
        }
    }

}

