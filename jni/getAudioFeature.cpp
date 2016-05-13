//
// Created by chaokong on 3/30/16.
//

#include "getAudioFeature.h"
#include <stdlib.h>
#include <stdio.h>
#include <vector>
#include <android/log.h>
#include "./DspFilters/Butterworth.h"


#define ENABLE_DEBUG 1


#if ENABLE_DEBUG
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, "getAudioFeature_RUN", __VA_ARGS__))
#else
#define LOGD(format,args...)
#endif

namespace getAudioFeature {




    void convCalculate(const float y[301], double *sound, double *match_sound, int numSound);

    int findMaximum(double *match_sound, int start, int stop, double &maximum);

    int ExceedThreshold(double *match_sound, int start, int stop, double threshold, double &Exceed);

    void average(double *match_sound, int *focus_start_list, int focus_count, int focus_duration, double *ave_focus);

    int findPeaks(double *ave_focus, int focus_duration, double peak_threshold);

    void getVarResult(double *ave_focus, int focus_duration, int step, double &mean_var, double &max_var);

    double getVar(double *ave_focus, int step);

    double getEnergy(double *ave_focus, int focus_duration);

    void exit_with_help() {
        printf(
                "Usage: ./getAudioFeature soundrawdata_file chirp_file feature_save_file\n"
        );
        exit(1);
    }

    int main(int argc, char **argv) {


        if (argc != 4) {
            exit_with_help();
        }

        LOGD( "start to get feature of audio file" );

        FILE *soundfile = NULL;
        FILE *chirpfile = NULL;
        FILE *featurefile = NULL;
        char *sound_filename = NULL;
        char *chirp_filename = NULL;
        char *feature_filename = NULL;
        sound_filename = argv[1];
        chirp_filename = argv[2];
        feature_filename = argv[3];

        int numSample = 0;
        int rawaudioSignals[200000];
        int value;


        int i2 = 0;
        float chirp[301];
        float chirp_value;

        if (sound_filename) {
            soundfile = fopen(sound_filename, "r");
            if (soundfile == NULL) {
                fprintf(stderr, "can't open file %s\n", sound_filename);
                exit(1);
            }
        }

        while (fscanf(soundfile, "%d", &value) > 0) {
            rawaudioSignals[numSample] = value;
            numSample++;
            if (numSample == 199998) {
                break;
            }
        }

        if (chirp_filename) {
            chirpfile = fopen(chirp_filename, "r");
            if (chirpfile == NULL) {
                fprintf(stderr, "can't open file %s\n", chirp_filename);
                exit(1);
            }
        }

        while (fscanf(chirpfile, "%f", &chirp_value) > 0) {
            chirp[i2] = chirp_value;
            i2++;
        }


        double *audio_signal = (double *) malloc(numSample * sizeof(double));
        if (audio_signal == NULL) {
            /* we have a problem */
            fprintf(stderr, "Error: out of memory.\n");
            return 1;
        }


        for (int j = 0; j < numSample; j++) {
            audio_signal[j] = (double) rawaudioSignals[j];
        }

        double *match_signal = (double *) malloc(numSample * sizeof(double));
        if (match_signal == NULL) {
            /* we have a problem */
            fprintf(stderr, "Error: out of memory.\n");
            return 1;
        }


        Dsp::SimpleFilter<Dsp::Butterworth::BandPass<5>, 1> f;
        f.setup(5,    // order
                48000,// sample rate
                15000, // center frequency
                3000);  // band width);
        f.process(numSample, &audio_signal);

//    FILE *outf_filter = fopen("filter1_0403_test2.txt", "w");
//
//    for ( int j=0; j<numSample; j++ )
//    {
//        //printf("%f\n", audio_signal[j]);
//        fprintf(outf_filter, "%f  ", audio_signal[j]);
//    }



        convCalculate(chirp, audio_signal, match_signal, numSample);

        FILE *outf_conv = fopen("convert1_0403_22_sound11.txt", "w");

//        for (int j = 0; j < numSample; j++) {
//            //printf("%f\n", audio_signal[j]);
//            fprintf(outf_conv, "%f  ", match_signal[j]);
//        }


//        printf("%d\n", numSample);

//    FILE *outf = fopen("results1_0307_test2.txt", "w");
//
//    for ( int j=0; j<numSample; j++ )
//    {
//        //printf("%f\n", audio_signal[j]);
//        fprintf(outf, "%f  ", audio_signal[j]);
//    }

        int duration1 = 2300;
        int duration2 = 2000;
        int f_start = 600;
        int f_end = 1500;
        int focus_duration = 900;
        int step = 100;
        int peak_threshold = 200;
        int threshold = 200000;
        int threshold2 = 2000000;

        double tmp_whole_max = 0;
        int tmp_whole_max_index = 0;

        double tmp_echo_max = 0;
        int tmp_echo_max_index = 0;

        double tmp_max = 0;
        int tmp_max_index = 0;

        double tmp_exceed_threshold = 0;
        int tmp_exceed_index = 0;

        int focus_start_list[10];
        int focus_end_list[10];
        int focus_count = 0;

        int whole_start = 0;


        tmp_whole_max_index = findMaximum(&match_signal[0], whole_start, numSample, tmp_whole_max);
//    printf("%d\n", tmp_whole_max_index);
//    printf("%f\n", tmp_whole_max);

        while (tmp_whole_max > threshold) {

            tmp_exceed_index = ExceedThreshold(&match_signal[0], whole_start, numSample, threshold,
                                               tmp_exceed_threshold);
//            printf("process raw sound file  %s\n", sound_filename);
//            printf("tmp_exceed_index  %d\n", tmp_exceed_index);
//            printf("tmp_whole_max  %f\n", tmp_whole_max);

            int tmp_start = tmp_exceed_index;
            int tmp_end = tmp_exceed_index + duration1;
            whole_start = tmp_end;
//            printf("whole_start  %d\n", whole_start);
            if ((tmp_start < 0) | (tmp_end > numSample)) {

                break;
            }
            tmp_max_index = findMaximum(&match_signal[0], tmp_start, tmp_end, tmp_max);
//            printf("tmp_max_index  %d\n", tmp_max_index);
//            printf("tmp_max  %f\n", tmp_max);


            int tmp_end2 = tmp_max_index + duration2;

            tmp_echo_max_index = findMaximum(&match_signal[0], (tmp_max_index + f_start), (tmp_max_index + f_end),
                                             tmp_echo_max);

            if ((tmp_end2 > tmp_end) || (tmp_echo_max > threshold2)) {
                tmp_whole_max_index = findMaximum(&match_signal[0], whole_start, numSample, tmp_whole_max);
                continue;
            }
            focus_start_list[focus_count] = tmp_max_index + f_start;
            focus_end_list[focus_count] = tmp_max_index + f_end;
            focus_count = focus_count + 1;


            tmp_whole_max_index = findMaximum(&match_signal[0], whole_start, numSample, tmp_whole_max);

        }

//        for (int i = 0; i < focus_count; i++) {
//            printf("%d\t", focus_start_list[i]);
//            printf("%d\n", focus_end_list[i]);
//
//        }

        double *ave_focus = (double *) malloc(focus_duration * sizeof(double));
        if (ave_focus == NULL) {
            /* we have a problem */
            printf("Error: out of memory.\n");
            return 1;
        }

        average(&match_signal[0], &focus_start_list[0], focus_count, focus_duration, &ave_focus[0]);

        double Energy = getEnergy(&ave_focus[0], focus_duration);

        int numPeaks = 0;
        numPeaks = findPeaks(&ave_focus[0], focus_duration, peak_threshold);
//        printf("\n\n%d\n", numPeaks);

        double mean_var = 0.0;
        double max_var = 0.0;

        getVarResult(&ave_focus[0], focus_duration, step, mean_var, max_var);
//        printf("%f\n", mean_var);
//        printf("%f\n", max_var);


        if (feature_filename) {
            featurefile = fopen(feature_filename, "w");
            if (featurefile == NULL) {
                fprintf(stderr, "can't open file %s\n", feature_filename);
                exit(1);
            }
        }


        fprintf(featurefile, "0 ");
        fprintf(featurefile, "1:%f ", Energy);
        fprintf(featurefile, "2:%d ", numPeaks);
        fprintf(featurefile, "3:%f ", mean_var);
        fprintf(featurefile, "4:%f\n", max_var);


//        printf("finish raw sound file  %s\n", sound_filename);

        free(audio_signal);
        free(match_signal);
        free(ave_focus);
//    fclose(outf);
//    fclose(outf_conv);
        fclose(soundfile);
        fclose(chirpfile);
        fclose(featurefile);

        return 0;


    }


    void convCalculate(const float y[301], double *sound, double *match_sound, int numSound) {
        int jC;
        int jA2;
        double s;
        int k;
        for (jC = 0; jC < numSound; jC++) {
            jA2 = jC + 151;
            if (numSound <= jA2) {
                jA2 = numSound;
            }

            s = 0.0;
            if (301 < jC + 152) {
                k = jC - 149;
            } else {
                k = 1;
            }

            while (k <= jA2) {
                s += sound[k - 1] * y[149 - (jC - k)];
                k++;
            }

            match_sound[jC] = s;
        }
    }

    int findMaximum(double *match_sound, int start, int stop, double &maximum) {
        double tmp_max = 0.0;
        int max_index = 0;
        for (int i = start; i < stop; i++) {
            //printf("%f\n", match_sound[i]);
            if (match_sound[i] > tmp_max) {
                tmp_max = match_sound[i];
                max_index = i;
            }
        }
        maximum = tmp_max;
        return max_index;
    }

    int ExceedThreshold(double *match_sound, int start, int stop, double threshold, double &Exceed) {
        int exceed_index = 0;
        for (int i = start; i < stop; i++) {
            if (match_sound[i] > threshold) {
                Exceed = match_sound[i];
                exceed_index = i;
                break;
            }
        }
        return exceed_index;
    }


    void average(double *match_sound, int *focus_start_list, int focus_count, int focus_duration, double *ave_focus) {
        for (int i = 0; i < focus_duration; i++) {
            double tmp_value = 0;
            for (int j = 0; j < focus_count; j++) {
                tmp_value = tmp_value + match_sound[focus_start_list[j] + i];

            }
            tmp_value = tmp_value / focus_count;
            ave_focus[i] = tmp_value;
        }

    }

    int findPeaks(double *ave_focus, int focus_duration, double peak_threshold) {
        int peak_number = 0;
        double prev = ave_focus[0];
        double cur = 0;
        double next = 0;
        int j = 0;
        for (int i = 1; i < (focus_duration - 1); i++) {
            cur = ave_focus[i];
            j = i + 1;
            next = ave_focus[j];

            if (((cur - next) > peak_threshold) & ((cur - prev) > peak_threshold)) {
                peak_number = peak_number + 1;
            }
            prev = cur;
        }
        return peak_number;
    }

    double getEnergy(double *ave_focus, int focus_duration) {
        double sum = 0;
        for (int i = 0; i < focus_duration; i++) {
            sum = sum + ave_focus[i] * ave_focus[i];
        }
        return sum;
    }

    void getVarResult(double *ave_focus, int focus_duration, int step, double &mean_var, double &max_var) {
        double tmp_var = 0;
        double all_var = 0.0;
        double tmp_max_var = 0;
        for (int i = 0; i < (focus_duration - step); i++) {
            tmp_var = getVar(&ave_focus[i], step);
            if (tmp_var > tmp_max_var) {
                tmp_max_var = tmp_var;
            }
            all_var = all_var + tmp_var;
        }

        max_var = tmp_max_var;
        mean_var = all_var / (focus_duration - step);
    }


    double getVar(double *ave_focus, int step) {
        double mean = 0.0;
        double var = 0.0;
        for (int i = 0; i < step; i++) {
            mean = mean + ave_focus[i];
        }
        mean = mean / step;

        for (int i = 0; i < step; i++) {
            var = var + (ave_focus[i] - mean) * (ave_focus[i] - mean);
        }
        var = var / (step - 1);
        return var;

    }
}
