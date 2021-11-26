package com.company;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int t = sc.nextInt();
        for (int i = 0; i < t; i++) {
            String s = sc.next();
            int count = 0;
            int length = s.length();
            if(length%3!=0){
                System.out.println("No");
                continue;
            }
            char[] chars = s.toCharArray();
            char temp = chars[0];
            for (int j = 3; j < chars.length; j=j+3) {
                if(temp!=chars[j]){
                    count++;
                    break;
                }
            }
            temp = chars[1];
            for (int j = 4; j < chars.length; j=j+3) {
                if(temp!=chars[j]){
                    count++;
                    break;
                }

            }
            temp = chars[3];
            for (int j = 5; j < chars.length; j=j+3) {
                if(temp!=chars[j]){
                    count++;
                    break;
                }

            }
            if(count>1){
                System.out.println("No");
            }else {
                System.out.println("Yes");
            }

        }
    }
}
