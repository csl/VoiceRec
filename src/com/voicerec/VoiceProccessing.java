package com.voicerec;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class VoiceProccessing {
    /** Called when the activity is first created. */
	
	static double alphar_PSD = 0.7;
	static int SetFrame = 10;
	static double gamma = 0.99;
	static double beta = 0.8;
	static double alphar_SNR = 0.8;
	static double Gmin = 0.1;
	
	static int FFT_SIZE = 128;		// Data frame size
	static int FFT_SIZE2 =	256;		// Data frame size x 2 = FFT size, half overlap
	static int FFT_SIZE_INDEX = 8;
	
	static  double TWOPI = 2* (Math.PI);
	
	String TAG = "VoiceRec";
	
	int firstset;

	// ------------ User define ----------- //
	static int SNR_TH = 10;  //0~10
	static int	X_sampling_rate = 8000;
	
	static COMPLEX W_FFT[]  = null;
	static int    WL = 0;
	
    static short  InOverlap[] = new short[FFT_SIZE];
    static short result[] = new short[FFT_SIZE];
	
	static double Hamming256[] = {
		    0.0800, 0.0801, 0.0806, 0.0813, 0.0822, 0.0835, 0.0850, 0.0868, 0.0889, 0.0913,
		    0.0939, 0.0968, 0.1000, 0.1034, 0.1071, 0.1111, 0.1153, 0.1198, 0.1245, 0.1295,
		    0.1347, 0.1402, 0.1459, 0.1519, 0.1581, 0.1645, 0.1712, 0.1781, 0.1852, 0.1925,
		    0.2001, 0.2078, 0.2157, 0.2239, 0.2322, 0.2407, 0.2494, 0.2583, 0.2673, 0.2765,
		    0.2859, 0.2954, 0.3051, 0.3149, 0.3249, 0.3350, 0.3452, 0.3555, 0.3659, 0.3765,
		    0.3871, 0.3979, 0.4087, 0.4196, 0.4305, 0.4416, 0.4527, 0.4638, 0.4750, 0.4863,
		    0.4976, 0.5089, 0.5202, 0.5315, 0.5428, 0.5542, 0.5655, 0.5768, 0.5881, 0.5993,
		    0.6106, 0.6217, 0.6329, 0.6439, 0.6549, 0.6659, 0.6767, 0.6875, 0.6982, 0.7088,
		    0.7193, 0.7297, 0.7400, 0.7501, 0.7601, 0.7700, 0.7797, 0.7893, 0.7988, 0.8081,
		    0.8172, 0.8262, 0.8350, 0.8436, 0.8520, 0.8602, 0.8683, 0.8761, 0.8837, 0.8912,
		    0.8984, 0.9054, 0.9121, 0.9187, 0.9250, 0.9311, 0.9369, 0.9426, 0.9479, 0.9530,
		    0.9579, 0.9625, 0.9669, 0.9710, 0.9748, 0.9784, 0.9817, 0.9847, 0.9875, 0.9899,
		    0.9922, 0.9941, 0.9958, 0.9972, 0.9983, 0.9991, 0.9997, 1.0000, 1.0000, 0.9997,
		    0.9991, 0.9983, 0.9972, 0.9958, 0.9941, 0.9922, 0.9899, 0.9875, 0.9847, 0.9817,
		    0.9784, 0.9748, 0.9710, 0.9669, 0.9625, 0.9579, 0.9530, 0.9479, 0.9426, 0.9369,
		    0.9311, 0.9250, 0.9187, 0.9121, 0.9054, 0.8984, 0.8912, 0.8837, 0.8761, 0.8683,
		    0.8602, 0.8520, 0.8436, 0.8350, 0.8262, 0.8172, 0.8081, 0.7988, 0.7893, 0.7797,
		    0.7700, 0.7601, 0.7501, 0.7400, 0.7297, 0.7193, 0.7088, 0.6982, 0.6875, 0.6767,
		    0.6659, 0.6549, 0.6439, 0.6329, 0.6217, 0.6106, 0.5993, 0.5881, 0.5768, 0.5655,
		    0.5542, 0.5428, 0.5315, 0.5202, 0.5089, 0.4976, 0.4863, 0.4750, 0.4638, 0.4527,
		    0.4416, 0.4305, 0.4196, 0.4087, 0.3979, 0.3871, 0.3765, 0.3659, 0.3555, 0.3452,
		    0.3350, 0.3249, 0.3149, 0.3051, 0.2954, 0.2859, 0.2765, 0.2673, 0.2583, 0.2494,
		    0.2407, 0.2322, 0.2239, 0.2157, 0.2078, 0.2001, 0.1925, 0.1852, 0.1781, 0.1712,
		    0.1645, 0.1581, 0.1519, 0.1459, 0.1402, 0.1347, 0.1295, 0.1245, 0.1198, 0.1153,
		    0.1111, 0.1071, 0.1034, 0.1000, 0.0968, 0.0939, 0.0913, 0.0889, 0.0868, 0.0850,
		    0.0835, 0.0822, 0.0813, 0.0806, 0.0801, 0.0800
		};
	
    public VoiceProccessing() 
    {
    	for (int i=0; i<FFT_SIZE; i++)
    	{
    		result[i] = 0;
    		InOverlap[i] = 0;
    	}
    	
    	firstset = 1;
	}
    
    void FFT(COMPLEX []data)
    {
    	int i,j,k,l,n1,n2;
    	double c,s,e,a,t1,t2;

    	
    	// Bit Reverse
    	j = 0; 
    	n2 = FFT_SIZE;
    	for (i=1; i < (FFT_SIZE2-1); i++)
    	{
    		n1 = n2;
    		while ( j >= n1 )
    		{
    			j = j - n1;
    			n1 = n1/2;
    		}
    		j = j + n1;
    	               
    		if (i < j)
    		{
    			t1 = data[i].real;
    			data[i].real = data[j].real;
    			data[j].real = t1;			
    	   }
    	}	

    	// FFT
    	n1 = 0; 
    	n2 = 1;
    	                                             
    	for (i=0; i < FFT_SIZE_INDEX; i++)
    	{
    		n1 = n2;
    		n2 = n2 + n2;
    		e = -TWOPI/n2;
    		a = 0.0;
    	                                             
    		for (j=0; j < n1; j++)
    		{
    			c = Math.cos(a);
    			s = Math.sin(a);
    			a = a + e;
    	                                            
    			for (k=j; k < FFT_SIZE2; k=k+n2)
    			{
    				t1 = c*data[k+n1].real - s*data[k+n1].image;
    				t2 = s*data[k+n1].real + c*data[k+n1].image;
    				data[k+n1].real = data[k].real - t1;
    				data[k+n1].image = data[k].image - t2;
    				data[k].real = data[k].real + t1;
    				data[k].image = data[k].image + t2;
    			}
    	   }
    	}
    }
    
    boolean InitW(int N)
    {
        int i;
        double t;
        if ( WL == N ) return true;    
    	//if ( W_FFT != null ) delete [] W_FFT;
        W_FFT = new COMPLEX[N];
    	for(i=0; i<FFT_SIZE2; i++)
    	{
    		W_FFT[i] = new COMPLEX();
    	}        
        
        if ( W_FFT != null ){
            WL = N;
            t = TWOPI/N;
            
            for ( i=0; i<N; i++ ){
                W_FFT[i].real = Math.cos( t*i );
                W_FFT[i].image = -Math.sin( t*i );
            }
            return true;
        }
        else{
            WL = 0;
            return false;
        }
    }

    //*****************************************//
    // Function: IFFT
    //*****************************************//
    void IFFT(COMPLEX []xin,int N)
    {
        int LH,m,i,j,l,le,B,ip,w_index;
        COMPLEX t1 = new COMPLEX(), t2 = new COMPLEX();

        if ( !InitW( N ) ) return;

        l=N;
        m=0;
        while (l == 0)
        {
        	l>>=1;
            m++;
        }
        	

        for (l=m;l>=1;l--)
        {
            le = (int)(1<<l);
            B = (le>>1);
            for (j=0;j<=B-1;j++)
            {
                w_index = (1<<(m-l))*j;
                for (i=j;i<=N-1;i=i+le)
                {
                    ip=i+B;
                    t1.real=(xin[i].real+xin[ip].real);
                    t1.image=(xin[i].image+xin[ip].image);
                    t2.real=(xin[i].real-xin[ip].real);
                    t2.image=(xin[i].image-xin[ip].image);
                    xin[ip].real=t2.real*W_FFT[w_index].real+t2.image*W_FFT[w_index].image;
                    xin[ip].image=t2.image*W_FFT[w_index].real-t2.real*W_FFT[w_index].image;
                    xin[i]=t1;
                }
            }
        }

        /* output reverse */
        for (LH=j=N>>1, i=1, m=N-2; i<=m; i++)
        {
            if (i<j){t1=xin[j]; xin[j]=xin[i]; xin[i]=t1;}
            l=LH;
            while (j>=l){j-=l; l>>=1;}
            j+=l;
        }

        /* scale output */
        for (i=0; i<N; i++)
        {
              
            //Log.i(TAG, "IFFT: " + i + "," + Double.toString(xin[i].real) + "," + Double.toString(xin[i].image));  
        	
            xin[i].real/=N;
            xin[i].image/=N;
        }
    }
    
    int FrameCnt = 0;
    short Overlap[] = new short[FFT_SIZE+1];
    
    void NoiseReduction(short Input[])
    {   // ------- internal variable -------- //
    	int i,j;
    	COMPLEX ch_f[] = new COMPLEX[FFT_SIZE2];
    	
    	double P_D[] = new double[FFT_SIZE+1];
    	double P_D_1[] = new double[FFT_SIZE+1];
    	double temp_reg = 0;
    	
    	double S_min_D[] = new double[FFT_SIZE+1];
    	double PostSNR[] = new double[FFT_SIZE+1];
    	double PriSNR[] = new double[FFT_SIZE+1];
    	
    	
    	double Gain[] =  new double[FFT_SIZE+1];
    	COMPLEX TmpOut[] = new COMPLEX[FFT_SIZE2];

    	Log.i(TAG, "NoiseReduction...");
    	for(i=0; i<FFT_SIZE2; i++)
    	{
    		ch_f[i] = new COMPLEX();
    		TmpOut[i] = new COMPLEX();
    	}
    	
    	// Hamming window
    	for(i=0; i<FFT_SIZE2; i++)
    	{
    		ch_f[i].real = (double) Input[i] * Hamming256[i];
    	}

    	Log.i(TAG, "before FFT...");
        // FFT 
    	FFT(ch_f);

    	Log.i(TAG, "after FFT...");
 /*   	
    	// Estimate P_D
    	if(FrameCnt < SetFrame)
    	{	for(i=0;i<FFT_SIZE+1;i++)
    		{	
        		//Log.i(TAG,  i + "," + Double.toString(ch_f[i].real) + "," + Double.toString(ch_f[i].image));

    			temp_reg = ch_f[i].real*ch_f[i].real + ch_f[i].image*ch_f[i].image;
    			P_D[i] = temp_reg;
    			P_D_1[i] = temp_reg;
    		}
    	}
    	else
    	{
    		for(i=0;i<FFT_SIZE+1;i++)
    		{
        		//Log.i(TAG,  i + "," + Double.toString(ch_f[i].real) + "," + Double.toString(ch_f[i].image));
    			
    			temp_reg = ch_f[i].real*ch_f[i].real + ch_f[i].image*ch_f[i].image;
    			P_D_1[i] = P_D[i];
    			P_D[i] = alphar_PSD*P_D[i] + (1-alphar_PSD)*temp_reg;
    		}
    	}

    	// Noise Floor
    	if(FrameCnt < SetFrame)
    	{	for(i=0;i<FFT_SIZE+1;i++)
    			S_min_D[i]=P_D[i];		
    	}
    	else
    	{	for(i=0;i<FFT_SIZE+1;i++)
    		{	if(S_min_D[i] < P_D[i])
    				S_min_D[i] = gamma*S_min_D[i] + (1-gamma)*(P_D[i]-beta*P_D_1[i])/(1-beta);
    			else
    				S_min_D[i] = P_D[i];
    		}
    	}

    	// Post SNR
    	for(i=0;i<FFT_SIZE+1;i++)
    	{	if(S_min_D[i]>0)
    			PostSNR[i] = P_D[i]/S_min_D[i];
    		else
    			PostSNR[i] = 0;
    	}

    	// PriSNR
    	for(i=0;i<FFT_SIZE+1;i++)
    	{	temp_reg = PostSNR[i]-SNR_TH;
    		if(temp_reg < 0)
    			temp_reg = 0;
    		PriSNR[i] = alphar_SNR*PriSNR[i] + (1-alphar_SNR)*temp_reg;
    	}

    	// Gain
    	for(i=0;i<FFT_SIZE+1;i++)
    	{	if(PriSNR[i]==0)
    			Gain[i] = 0;
    		else
    			Gain[i] = PriSNR[i]/(1+PriSNR[i]);
    		if(Gain[i]< Gmin)
    			Gain[i] = Gmin;
    	}

    	// Output
    	for(i=0;i<FFT_SIZE+1;i++)  
    	{	
    		TmpOut[i].real=(double) Gain[i]*ch_f[i].real;
    		TmpOut[i].image=(double) Gain[i]*ch_f[i].image;
    		//TmpOut[i].real= (double) ch_f[i].real;
    		//TmpOut[i].image= (double) ch_f[i].image;
    		//Log.i(TAG,  i + "," + Double.toString(TmpOut[i].real) + "," + TmpOut[i].image);
    	}

    	// Conjugate Symmetric
    	for(i=1;i<FFT_SIZE;i++)
    	{   
    		TmpOut[FFT_SIZE2-i].real = TmpOut[i].real;
    		TmpOut[FFT_SIZE2-i].image=(-1) * TmpOut[i].image;
    	}
    	
    	Log.i(TAG, "Before IFFT...");
*/
    	
    	// Overlap half frame
    	/*
        for(i=0;i<FFT_SIZE2;i++) 
    	{   
        	Log.i(TAG,  i + " ," + Double.toString(ch_f[i].real) + "," + Double.toString(ch_f[i].image));
    	} */   	
    	// IFFT
     	IFFT(ch_f, FFT_SIZE2);  
	
    	// Overlap half frame
        for(i=0;i<FFT_SIZE;i++) 
    	{   
        	//Log.i(TAG,  i + " ," + Double.toString(ch_f[i].real) + "," + Short.toString(Overlap[i]));
        	result[i] = (short) (ch_f[i].real + Overlap[i]);
            Overlap[i] = (short) ch_f[i+FFT_SIZE].real;   
    	}

    	if(FrameCnt<SetFrame)
    		FrameCnt++;  
    	
    	Log.i(TAG, "after FFT_SIZE...");
    } 
    

    
    public void proccess_running(short[] voice_data, int Sizes, DataOutputStream dos)
    {
    	short  InData[] = new short[FFT_SIZE];
    	short ch[] = new short[FFT_SIZE2];

    	Log.i(TAG, "proccess_running...");
    	
    	for (int i=0; i<voice_data.length; i+=FFT_SIZE)
		{	
    		
			for(int j=0;j<FFT_SIZE;j++)
			{	// Read data from input wav file
				InData[j] = voice_data[i + j];
			}
			if (firstset == 1)
			{
				firstset = 0;
				for(int j=0;j<FFT_SIZE;j++)
				{	// Read data from input wav file
					InOverlap[j] = voice_data[FFT_SIZE + j];
				}
			}
			
		
			// STFT process
			//memcpy(&ch[0],InOverlap,CpySize);
			//memcpy(&ch[FFT_SIZE],InData,CpySize);
	
			int count=0;
			int j;
			
			for (j=0; j<FFT_SIZE; j++)
			{
				ch[j] = InOverlap[count];
				count++;
				
				Log.i(TAG, "input: " + ch[j]);
			}
			
			//	ch[j] = InData[count];
			
			count=0;
			for (j=FFT_SIZE; j<FFT_SIZE2; j++)
			{
				ch[j] = voice_data[count];
				count++;
				Log.i(TAG, "input: " + ch[j]);
			}	
		
			// Noise Reduction
			NoiseReduction(ch);
			Log.i(TAG, "after NoiseReduction...");
			
			// overlap
			//memcpy(InOverlap,InData,CpySize);
			for (j=0; j< FFT_SIZE; j++)
			{
				InOverlap[j] = InData[j];
			}
			
			// Write output wav file
			for (int k=0; k<FFT_SIZE ; k++)
			{
				try {
					Log.i(TAG, Short.toString(result[k]));
					dos.writeShort(result[k]);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			//InCnt = InCnt+FFT_SIZE;
			//if(InCnt%X_sampling_rate==0)
				//printf(".");
		} 
    }
   
	
}