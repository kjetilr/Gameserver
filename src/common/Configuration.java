package common;

/**
 * Incredibly lazy implementation. Will put in XML when I'm feeling less lazy.
 */
public class Configuration {
    private static Configuration ourInstance = new Configuration();

    public static Configuration getInstance() {
        return ourInstance;
    }

    private Configuration() {
    }

    public int getServerPort() {
        String paramValue = System.getProperty("network.port");
        if (paramValue != null) {
            return Integer.parseInt(paramValue);
        }
        return 9070;
    }

    public long getUpdateInterval() {
        String paramValue = System.getProperty("system.updateinterval");
        if (paramValue != null) {
            return Integer.parseInt(paramValue);
        }
        return 100; //In ms
    }

    public int getNumThreads() {
        String paramValue = System.getProperty("system.numthreads");
        if (paramValue != null) {
            return Integer.parseInt(paramValue);
        }
        return 8;
    }

    public String getServerURL() {
        String paramValue = System.getProperty("network.serverurl");
        if (paramValue != null) {
            return paramValue;
        }
        return "localhost";
    }

    public boolean isLoggingData() {
        String paramValue = System.getProperty("system.logdata");
        if (paramValue != null) {
            return Boolean.parseBoolean(paramValue);
        }
        return false;
    }

    public String getTraceFileName() {
        String paramValue = System.getProperty("system.tracefilename");
        if (paramValue != null) {
            return paramValue;
        }
        return "src/logs/5min-5players.log";
    }

    public boolean isSingleThreaded() {
        String paramValue = System.getProperty("system.singlethreaded");
        if (paramValue != null) {
            return Boolean.parseBoolean(paramValue);
        }
        return false;
    }

    // How many array elements to process for each load
    public int getLoadIntensity() {
        String paramValue = System.getProperty("load.intensity");
        if (paramValue != null) {
            return Integer.parseInt(paramValue);
        }
        return 1000;
    }

    public int getLoadVectorSize() {
        String paramValue = System.getProperty("load.vectorsize");
        if (paramValue != null) {
            return Integer.parseInt(paramValue);
        }
        return 10000000;
    }


    public String getDelayStatisticsFileName() {
        String paramValue = System.getProperty("system.delaystatisticsfilename");
        if (paramValue != null) {
            return paramValue;
        }
        return "delay_statistics.txt";
    }

    public String getCPUStatisticsFileName() {
        String paramValue = System.getProperty("system.cpustatisticsfilename");
        if (paramValue != null) {
            return paramValue;
        }
        return "cpu_statistics.txt";
    }

    public int getNumSelectors() {
        String paramValue = System.getProperty("system.numselectors");
        if (paramValue != null) {
            return Integer.parseInt(paramValue);
        }
        return getNumThreads();
    }

    public long getKillTime() {
        String paramValue = System.getProperty("system.killtime");
        if (paramValue != null) {
            return Integer.parseInt(paramValue);
        }
        return Long.MAX_VALUE;
    }

    public int getMessageQueueLength() {
        return 10000;
    }

    public int getNetMessageQueueLength() {
        return 10000;
    }

    public int getChangeReqQueueLength() {
        return 100000;
    }

    public int getLogonDelay() {
        String paramValue = System.getProperty("playback.logondelay");
        if (paramValue != null) {
            return Integer.parseInt(paramValue);
        }
        return 50;
    }

    public int getStartupClients() {
        String paramValue = System.getProperty("playback.startnumber");
        if (paramValue != null) {
            return Integer.parseInt(paramValue);
        }
        return 0;
    }

    public String getTimeoutsFileName() {
        String paramValue = System.getProperty("system.timeoutsfilename");
        if (paramValue != null) {
            return paramValue;
        }
        return "timeouts_statistics.txt";
    }

    public boolean showGui() {
        String paramValue = System.getProperty("system.usegui");
        if (paramValue != null) {
            return Boolean.parseBoolean(paramValue);
        }
        return true;
    }

    public int getWindowSize() {
        String paramValue = System.getProperty("client.windowsize");
        if (paramValue != null) {
            return Integer.parseInt(paramValue);
        }
        return 500;
    }
}
