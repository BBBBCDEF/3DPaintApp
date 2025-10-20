package app.util;

import java.awt.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class RegistryManager {
    private static final Preferences prefs = Preferences.userNodeForPackage(RegistryManager.class);
    private static final String KEY_MAX_FPS = "maxFps";
    private static final String KEY_UPSCALING_RATIO = "upscalingRatio";
    private static final String KEY_REFLECTION_MAX_DEPTH = "reflectionMaxDepth";
    private static final String KEY_BACKGROUND_COLOR = "backgroundColor";
    private static final String KEY_SHOW_DEBUG_INFO = "showDebugInfo";
    private static final String KEY_BENCHMARK_SCORE = "benchmarkScore";

    public static void saveMaxFps(int maxFps) {
        prefs.putInt(KEY_MAX_FPS, maxFps);
    }
    public static void saveUpscalingRatio(int upscalingRatio) {
        prefs.putInt(KEY_UPSCALING_RATIO, upscalingRatio);
    }
    public static void saveReflectionMaxDepth(int reflectionMaxDepth) {
        prefs.putInt(KEY_REFLECTION_MAX_DEPTH, reflectionMaxDepth);
    }
    public static void saveBackgroundColor(Color backgroundColor) {
        prefs.putInt(KEY_BACKGROUND_COLOR + "Red", backgroundColor.getRed());
        prefs.putInt(KEY_BACKGROUND_COLOR + "Green", backgroundColor.getGreen());
        prefs.putInt(KEY_BACKGROUND_COLOR + "Blue", backgroundColor.getBlue());
    }
    public static void saveShowDebugInfo(boolean showDebugInfo) {
        prefs.putBoolean(KEY_SHOW_DEBUG_INFO, showDebugInfo);
    }
    public static void saveBenchmarkScore(int[] benchmarkScores){
        prefs.putInt(KEY_BENCHMARK_SCORE + "Length", benchmarkScores.length);
        for(int i = 0; i < benchmarkScores.length; i++) {
            prefs.putInt(KEY_BENCHMARK_SCORE + i, benchmarkScores[i]);
        }
    }

    public static int loadMaxFps(int defaultValue) {
        return prefs.getInt(KEY_MAX_FPS, defaultValue);
    }
    public static int loadUpscalingRatio(int defaultValue) {
        return prefs.getInt(KEY_UPSCALING_RATIO, defaultValue);
    }
    public static int loadReflectionMaxDepth(int defaultValue) {
        return prefs.getInt(KEY_REFLECTION_MAX_DEPTH, defaultValue);
    }
    public static Color loadBackgroundColor(Color defaultValue) {
        return new Color(
                prefs.getInt(KEY_BACKGROUND_COLOR + "Red", defaultValue.getRed()),
                prefs.getInt(KEY_BACKGROUND_COLOR + "Green", defaultValue.getGreen()),
                prefs.getInt(KEY_BACKGROUND_COLOR + "Blue", defaultValue.getBlue())
        );
    }
    public static boolean loadShowDebugInfo(boolean defaultValue) {
        return prefs.getBoolean(KEY_SHOW_DEBUG_INFO, defaultValue);
    }
    public static int[] loadBenchmarkScore() {
        int[] scores = new int[prefs.getInt(KEY_BENCHMARK_SCORE + "Length", 0)];
        for(int i = 0; i < scores.length; i++) {
            scores[i] = prefs.getInt(KEY_BENCHMARK_SCORE + i, 0);
        }
        return scores;
    }
    public static void allClear() {
        try {
            prefs.clear();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

}

