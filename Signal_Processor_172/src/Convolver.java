/**
Convolve-J - A simple WAV file CLI convolution utility in java.
Copyright (C) 2012  Chuck Ritola

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.File;

import javax.sound.sampled.AudioSystem;

import com.ritolaaudio.simplewavio.Utils;
 
/**
 * Performs a convolution operation on a WAV file as input, with a WAV file as impulse,<br> 
 * normalizes the result, and writes it to a 16-bit little-endian signed PCM WAV/RIFF file.<br>
 * Multichannel and up to 32 bits input should be acceptable.
 * @author chuck
 *
 */
public final class Convolver
        {
        public static final void convolve(File input, File impulse, File output, int numThreads)
                {
                try{
                final float [][] imp = Utils.WAVToFloats(impulse);
                final float [][] in = Utils.WAVToFloats(input);
                float [][] out= new float[(in.length+imp.length)][AudioSystem.getAudioFileFormat(input).getFormat().getChannels()];
                
                //Init/Zero the output
                for(int s=0; s<out.length; s++)
                        {
                        for(int c=0; c<out[0].length; c++)
                                {
                                out[s][c]=0;
                                }//end for(c)
                        }//end for(s)
                
                //Removed in favor of normalizing the output before it is encoded.
                /*      
                //This makes sure that the impulse doesn't overdrive everything.
                //normalizeImpulse(imp);
                */
                
                //Split the work to threads
                int threadBufferSize=in.length/numThreads;
                Thread [] workThreads = new Thread[numThreads];
                for(int t=0; t<numThreads; t++)
                        {
                        workThreads[t] = convolveNonBlocking(t*threadBufferSize,threadBufferSize,in,imp,out,t);
                        }
                
                //Wait for threads to finish
                for(Thread t:workThreads)
                        {
                        if(t.isAlive())
                                {
                                try{t.join();}
                                catch(InterruptedException e){e.printStackTrace();}
                                }
                        }//end for(workThreads)
                
                normalize(out);
                System.out.println("Writing result...");
                Utils.floatsToWAV(out, output,AudioSystem.getAudioFileFormat(input).getFormat().getSampleRate());
                }
                catch(Exception e){e.printStackTrace();}
                System.out.println("Convolution complete.");
                }//end convolve(...)
        
        private static final Thread convolveNonBlocking(final int startIndex, final int blockSize, final float [][] in, final float [][]imp, final float [][] out, final int threadNumber)
                {
                Thread result = new Thread()
                        {
                        @Override
                        public void run()
                                {
                                for(int i=startIndex;i<blockSize+startIndex; i++)
                                        {convolveInPlace(in[i],i,in,imp,out,threadNumber);}
                                }//end run()
                        };
                result.start();
                return result;
                };
        
        public static final void normalize(float [][] audioToNormalize)
                {
                System.out.println("Normalizing the output...");
                System.out.println("    Finding peak...");
                float peak = 0;
                for(float [] frame:audioToNormalize)
                        {
                        for(int channel=0; channel<frame.length;channel++)
                                {
                                final float val=Math.abs(frame[channel]);
                                if(val>peak)    peak=val;
                                }//end for(channel)
                        }//end for(audioToNormalize)
                
                //Apply gain adjustment
                System.out.println("    Peak found at "+peak);
                System.out.println("    Applying gain adjustment...");
                for(float [] frame:audioToNormalize)
                        {
                        for(int channel=0; channel<frame.length;channel++)
                                {
                                frame[channel]/=peak;
                                }//end for(channel)
                        }//end for(audioToNormalize)
                System.out.println("Normalization complete.");
                }//end normalize(...)
                
        public static final void normalizeImpulse(float [][] impulse)
                {
                System.out.println("Normalizing the impulse...");
                final double [] accumulator = new double[impulse[0].length];
                for(int c=0; c<accumulator.length;c++){accumulator[c]=1;}
                //Get magnitude of the impulse
                System.out.println("\tGetting the energy of the impulse...");
                for(float [] frame:impulse)
                        {
                        //if(frame[0]>.9)System.out.println("?!?!  "+frame[0]);
                        for(int c=0; c<frame.length; c++)
                                {
                                accumulator[c]+=Math.abs(frame[c]);
                                }//end for()
                        }//end for(impulse)
                //System.out.println("accumulator[1] "+accumulator[1]);
                System.out.println("\tDividing the impulse by the energy...");
                //Normalize
                for(float [] frame:impulse)
                        {
                        for(int c=0; c<frame.length; c++)
                                {
                                frame[c]/=accumulator[c];
                                }//end for()
                        }//end for(impulse)
                System.out.println("Normalizing complete.");
                }//end normalizeImpulse()
                
        private static final void convolveInPlace(float []multiplier, int startIndex, float [][] in, float [][] impulse, float [][] out, int threadNumber)
                {
                if(startIndex % 1024 == 0)System.out.println("Convolving frame "+startIndex+" in thread "+threadNumber);
                int impIndex=0;
                //float [] reverbFrame = new float[multiplier.length];
                if(multiplier.length==2)
                        {//optimized
                        final float m0=multiplier[0];
                        final float m1=multiplier[1];
                        final int len=out.length;
                        for(int sample=startIndex; sample<len; sample++)
                                {
                                out[sample][0]+=(m0*(impulse[impIndex][0]));
                               
                                out[sample][1]+=(m1*(impulse[impIndex][1]));
                               
                                impIndex++;
                                if(impIndex>=impulse.length)break;
                                }//end for(sample)
                        }
                else
                        {//Not-so-optimized
                        for(int sample=startIndex; sample<in.length; sample++)
                                {
                                for(int channel = 0; channel<multiplier.length; channel++)
                                        {
                                        out[sample][channel]=out[sample][channel]+(multiplier[channel]*(impulse[impIndex][channel]));
                                        }
                                impIndex++;
                                if(impIndex>=impulse.length)break;
                                }//end for(sample)
                        }//end else{non-optimized}
                }//end convolveInPlace(...)
        }//end Convolver