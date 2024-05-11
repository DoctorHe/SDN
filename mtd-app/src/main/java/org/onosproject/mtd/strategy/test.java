package org.onosproject.mtd.strategy;

import org.onlab.packet.IpAddress;

import java.util.Arrays;

public class test {
    public static void main(String[] args) {
        ShiftAddress shiftAddress = new ShiftAddress();
        Thread thread = new Thread(shiftAddress);
        thread.start();
    }
    public static int[] chances(float[][] hfrMatrix,int p){
        float sh=0;
        float tempHost=0;
        int s=0;
        if(p==0){
            s=16;
        }
        else if(p==16){
            s=19;
        }
        else s=21;
        for(int i=p;i<s;i++) {
            for (int j = 0; j < 4; j++) {
                sh+=hfrMatrix[i][j];
            }
        }
        float rh= (float) Math.random()*sh;
        boolean flag =false;//to exit two loop bodies
        for(int i=p;i<s;i++) {
            for (int j = 0; j < 4; j++) {
                tempHost+=hfrMatrix[i][j];
                if(tempHost>=rh){
                    flag=true;
                    return new int[]{i,j};
                }
            }
            if (flag==true){
                break;
            }
        }
        return null;
   }


}
class ShiftAddress implements Runnable{
    @Override
    public void run() {

        MtdMechanism mtdMechanism=new MtdMechanism();

        //1-16 host  //17-19 server  //20-21 database
        mtdMechanism.export();
        while(true){
            int[] host=chances(mtdMechanism.hfrMatrix,0);

            if (host[1]==0){
                //Splicing Strings to form host Ip addresses
                System.out.println(Arrays.toString(host));
                String s=(121+((host[0])/4))+".0.0."+(1+((host[0]%4)));
                System.out.println(s);

            }else if(host[1]==1){

            }else if(host[1]==2){

            }else {

            }

            int[] server=chances(mtdMechanism.hfrMatrix,16);
            System.out.println("chance server:"+ (server[0]+1) +",    mtd mechanism:" + (server[1]+1));

            int[] database=chances(mtdMechanism.hfrMatrix,19);
            System.out.println("chance database:"+ (database[0]+1) +",    mtd mechanism:" + (database[1]+1));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static int[] chances(float[][] hfrMatrix,int p){
        float sh=0;
        float tempHost=0;
        int s=0;
        if(p==0){
            s=16;
        }
        else if(p==16){
            s=19;
        }
        else s=21;
        for(int i=p;i<s;i++) {
            for (int j = 0; j < 4; j++) {
                sh+=hfrMatrix[i][j];
            }
        }
        float rh= (float) Math.random()*sh;
        boolean flag =false;//to exit two loop bodies
        for(int i=p;i<s;i++) {
            for (int j = 0; j < 4; j++) {
                tempHost+=hfrMatrix[i][j];
                if(tempHost>=rh){
                    flag=true;
                    return new int[]{i,j};
                }
            }
            if (flag==true){
                break;
            }
        }
        return null;
    }
}

