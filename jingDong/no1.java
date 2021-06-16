package com.company;


import java.util.*;

public class Main {
    private int row = 0;
    private int[][] result;
    private int count;
    public Main(int[] array){
        this.row = 0;
        this.result = new int[this.factor(array.length)][array.length];
    }
    public int factor(int a){
        int r = 1;
        for (;a>=1;a--){
            r*=a;
        }
        return r;
    }
    public void perm(int[] array,int start){
        if(start == array.length){
            int flag = 0;
            for (int i = 0; i < array.length; i++) {
                this.result[row][i]=array[i];
                if (i< array.length-1 && Math.abs(((array[i+1]-array[i])))>2){
                    flag=1;
                    break;
                }
            }
            if(flag==0 && array[array.length-1]==array.length-1 &&array[0]==0){
                count++;
            }
        }
        else {
            for (int i = start; i <array.length ; i++) {
                swap(array,start,i);
                perm(array,start+1);
                swap(array,start,i);
            }
        }
    }
    public void swap(int[] array,int s,int i){
        int t = array[s];
        array[s]= array[i];
        array[i]=t;
    }
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] a = new int[n+1];
        for (int i = 0; i < a.length; i++) {
            a[i]=i;
        }
        Main p = new Main(a);
        p.perm(a,0);
        System.out.println(p.count%998244353);




    }
}