package org.onosproject.mtd.strategy;


import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MtdMechanism {

    public static boolean ipMtdSign =true;
    public static boolean portMtdSign=false;
    public static boolean pathMtdSign=true;
    public static boolean hostMtdSign=true;
    public static int[][] mtdMatrix = {
            {0,0,0,0,1,1,1,1,2,2,2,2,3,3,3,3,4,4,4,5,5},
            {0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,0,1}};

    //Host included in the server
    public static Set<Integer> serverHasHosts1= new HashSet<>();
    public static Set<Integer> serverHasHosts2= new HashSet<>();
    public static Set<Integer> serverHasHosts3= new HashSet<>();
    public static Set<Integer> databaseHasServer1= new HashSet<>();
    public static Set<Integer> databaseHasServer2= new HashSet<>();
    //define the path host-server-database;
    public static void initSHH(){
        serverHasHosts1.add(1);
        serverHasHosts1.add(2);
        serverHasHosts1.add(3);
        serverHasHosts1.add(4);
        serverHasHosts1.add(5);
        serverHasHosts2.add(6);
        serverHasHosts2.add(7);
        serverHasHosts2.add(8);
        serverHasHosts2.add(9);
        serverHasHosts2.add(10);
        serverHasHosts3.add(11);
        serverHasHosts3.add(12);
        serverHasHosts3.add(13);
        serverHasHosts3.add(14);
        serverHasHosts3.add(15);
        serverHasHosts3.add(16);
        databaseHasServer1.add(1);
        databaseHasServer1.add(2);
        databaseHasServer2.add(2);
        databaseHasServer2.add(3);
    }

    public int hostNumber ;
    //Vulnerabilities in the system
    public  float[] v={6.5F, (float) 6.8, (float) 9.8, 7.5F, 5.0F, 6.5F, (float) 7.1, (float) 7.1};
    //the interconnection between vulnerabilities
//    public float[]
    //the probability of successfully breaking through vulnerabilities
    public float[] pev=new float[8];
    //vulnerabilities present on each host,[12][3]=1 host13 has vulenerabilities 4,[12][2]=0  host13 not has vulenerabilities 3
    public int[][] hhv= {
            {1,0,1,0,1,1,1,0},{0,1,0,0,1,0,1,0},{0,1,0,0,0,1,0,1},{0,0,1,0,0,0,0,1},
            {1,0,1,0,1,0,1,0},{0,1,0,0,1,0,1,0},{0,1,1,0,0,1,1,0},{0,0,1,0,0,1,0,1},
            {1,0,1,0,0,0,1,0},{0,1,0,0,0,1,1,0},{0,1,0,0,0,1,0,1},{0,0,1,0,0,0,1,0},
            {1,0,1,0,1,0,1,0},{0,1,0,0,1,0,1,0},{0,1,1,0,0,1,1,0},{0,0,1,0,1,0,0,1}};

    //the probability of each host being breached
    public float[] phb=new float[16];

    //the probability of each host being selected
    public float[] phs=new float[16];

    //the probability of  servers being selected,5,5,6
    public float[] pss=new float[3];
    //the probability of databases being selected,12,23
    public float[] pds=new float[2];

    //The selection probability of different MTD mechanisms on the host for jump frequency
    public float[] pmh={0.5F, (float) 0.1, (float) 0.2, (float) 0.2};
    //The selection probability of different MTD mechanisms on the server for jump frequency
    public float[] pms={0.2F, (float) 0.4, (float) 0.2, (float) 0.2};
    //The selection probability of different MTD mechanisms on the database for jump frequency
    public float[] pmd={0.2F, (float) 0.4, (float) 0.2, (float) 0.2};

    //final result matrix
    public float[][] hfrMatrix= new float[21][4];


    //calculate the probability of successfully breaking through vulnerabilities

    public void cbv(){
        for(int i=0;i<8;i++){
            pev[i]=v[i]/10;
        }
//        System.out.println(Arrays.toString(pev));
    }

    //calculate the probability of each host being breached

    public void phb(){
        for(int i=0;i<16;i++){
            phb[i]=1;
            for(int j=0;j<8;j++){
                if(hhv[i][j]==1){
                    phb[i]=phb[i]*pev[j];
                }
            }

            //keep two decimal places
            BigDecimal b = new BigDecimal(phb[i]);
            phb[i] = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue(); //ROUND_HALF_UP表明四舍五入，ROUND_HALF_DOWN表明五舍六入，2：保留两位小数
        }
//        System.out.println(Arrays.toString(phb));
    }

    //calculate the probability of each host being selected
    public void phs(){
        float s=0;
        for(int i=0;i<16;i++){
            s+=phb[i];
        }
        for(int i=0;i<16;i++){
            phs[i]=phb[i]/s;
            //keep two decimal places
            BigDecimal b = new BigDecimal(phs[i]);
            phs[i] = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue(); //ROUND_HALF_UP表明四舍五入，ROUND_HALF_DOWN表明五舍六入，2：保留两位小数
        }
//        System.out.println(Arrays.toString(phs));
    }

    //calculate the probability of each servers being selected
    public void pss(){
        float s1=0,s2=0,s3=0;
        for(int i=0;i<16;i++){
            if(i<5){
                s1+=phs[i];
            }
            else if(i<10&&i>=5){
                s2+=phs[i];
            }
            else s3+=phs[i];
        }

        pss[0]=s1/(s1+s2+s3);
        //keep two decimal places
        BigDecimal b = new BigDecimal(pss[0]);
        pss[0] = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue(); //ROUND_HALF_UP表明四舍五入，ROUND_HALF_DOWN表明五舍六入，2：保留两位小数
        pss[1]=s1/(s1+s2+s3);
        //keep two decimal places
        BigDecimal b1 = new BigDecimal(pss[1]);
        pss[1] = b1.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue(); //ROUND_HALF_UP表明四舍五入，ROUND_HALF_DOWN表明五舍六入，2：保留两位小数
        pss[2]=s1/(s1+s2+s3);
        //keep two decimal places
        BigDecimal b2 = new BigDecimal(pss[2]);
        pss[2] = b2.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue(); //ROUND_HALF_UP表明四舍五入，ROUND_HALF_DOWN表明五舍六入，2：保留两位小数

        pds[0]=(pss[0]+pss[1])/(pss[0]+pss[1]+pss[2]+pss[1]);
        //keep two decimal places
        BigDecimal b3 = new BigDecimal(pds[0]);
        pds[0] = b3.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue(); //ROUND_HALF_UP表明四舍五入，ROUND_HALF_DOWN表明五舍六入，2：保留两位小数

        pds[1]=(pss[2]+pss[1])/(pss[0]+pss[1]+pss[2]+pss[1]);
        //keep two decimal places
        BigDecimal b4 = new BigDecimal(pds[1]);
        pds[1] = b4.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue(); //ROUND_HALF_UP表明四舍五入，ROUND_HALF_DOWN表明五舍六入，2：保留两位小数

//        System.out.println(Arrays.toString(pss));
//        System.out.println(Arrays.toString(pds));
    }

    //Final result frequency matrix
    public void hfrMatrix(){
        for(int i=0;i<16;i++){
            for(int j=0;j<4;j++){
                hfrMatrix[i][j]=phs[i]*pmh[j];
            }
        }
        for(int i=0;i<3;i++){
            for(int j=0;j<4;j++){
                hfrMatrix[i+16][j]=pss[i]*pms[j];
            }
        }
        for(int i=0;i<2;i++){
            for(int j=0;j<4;j++){
                hfrMatrix[i+19][j]=pds[i]*pmd[j];
            }
        }
        int count=1;
        for(int i=0;i<21;i++) {
            for (int j = 0; j < 4; j++) {
//                System.out.print(hfrMatrix[i][j]+"\t");
            }
//            System.out.println(count+++"\t");
        }

    }

    public void export(){
        cbv();
        phb();
        phs();
        pss();
        hfrMatrix();
    }
}
