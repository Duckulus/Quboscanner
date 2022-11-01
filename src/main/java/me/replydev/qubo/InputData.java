package me.replydev.qubo;

import inet.ipaddr.IPAddressSeqRange;
import inet.ipaddr.IPAddressString;
import me.replydev.utils.FileUtils;
import me.replydev.utils.InvalidRangeException;
import me.replydev.utils.IpList;
import me.replydev.utils.PortList;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class InputData {

    private IpList ipList;
    private PortList portrange;
    private IpList[] ipRanges;


    private CommandLine cmd;

    private boolean ping;
    private final String filename;

    private final Options options;

    private Options buildOptions() {
        Option iprange = new Option("range", "iprange", true, "The IP range that me.replydev.qubo will scan");

        Option rangeFile = new Option("rangefile", "iprangefile", true, "File with multiple Ip Ranges");

        Option backup = new Option("bu", "backup", false, "Wether to load backup from Database");

        Option portrange = new Option("ports", "portrange", true, "The range of ports that me.replydev.qubo will work on");
        portrange.setRequired(true);

        Option threads = new Option("th", "threads", true, "Maximum number of running async threads");
        threads.setRequired(true);

        Option timeout = new Option("ti", "timeout", true, "Server Ping timeout");
        timeout.setRequired(true);

        Option count = new Option("c", "pingcount", true, "How many times me.replydev.qubo will ping a server");
        count.setRequired(false);

        Option noping = new Option("noping", false, "Prevent Qubo from pinging IPs before start scan");
        noping.setRequired(false);

        Option nooutput = new Option("nooutput", false, "Prevent Qubo from creating output file");
        nooutput.setRequired(false);

        Option all = new Option("all", false, "Force Qubo to scan broadcast IPs and common ports");
        all.setRequired(false);

        Option fulloutput = new Option("fulloutput", false, "Create a more readable but bigger output file");
        fulloutput.setRequired(false);

        Option filterVersion = new Option("ver", "filterversion", true, "Show only hits with given version");
        filterVersion.setRequired(false);

        Option filterMotd = new Option("motd", "filtermotd", true, "Show only hits with given motd");
        filterMotd.setRequired(false);

        Option filterOn = new Option("on", "minonline", true, "Show only hits with at least <arg> players online");
        filterOn.setRequired(false);

        Option debug = new Option("d", "debug", false, "Enables debug mode");
        debug.setRequired(false);

        Options options = new Options();
        options.addOption(iprange);
        options.addOption(rangeFile);
        options.addOption(backup);
        options.addOption(portrange);
        options.addOption(threads);
        options.addOption(timeout);
        options.addOption(count);
        options.addOption(noping);
        options.addOption(nooutput);
        options.addOption(all);
        options.addOption(fulloutput);
        options.addOption(filterVersion);
        options.addOption(filterMotd);
        options.addOption(filterOn);
        options.addOption(debug);

        return options;
    }

    public void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("-range <arg> -ports <arg> -th <arg> -ti <arg>", options);
        System.exit(-1);
    }

    public InputData(String[] command, Optional<String> startHost) throws InvalidRangeException, NumberFormatException {
        options = buildOptions();
        CommandLineParser parser = new DefaultParser();
        String ipStart = "", ipEnd = "";
        try {
            cmd = parser.parse(options, command);

            if (startHost.isPresent() && cmd.hasOption("bu")) {
                ipStart = startHost.get();
                ipEnd = "255.255.255.255";
            } else if (cmd.hasOption("range")) {

                try {
                    //Check for begin-end range first, first split the string
                    String[] beginEnd = cmd.getOptionValue("range").split("-");

                    if (beginEnd.length >= 2) {   //See if its length is 2 (begin-end)

                        ipStart = cmd.getOptionValue("range").split("-")[0];
                        ipEnd = cmd.getOptionValue("range").split("-")[1];


                    }

                    //Transform hostname into an IP
                    if (Objects.equals(ipStart, "")) {
                        try {
                            ipStart = InetAddress.getByName(cmd.getOptionValue("range")).getHostAddress();
                            ipEnd = ipStart;
                        } catch (UnknownHostException ignored) {
                        }
                    }

                    //Checks if the string split are both IPs. If not, IPAddressString parses them as a CIDR or shorthand range.
                    if (IpList.isNotIp(ipStart) || IpList.isNotIp(ipEnd)) {
                        IPAddressSeqRange range = new IPAddressString(cmd.getOptionValue("range")).getSequentialRange();
                        ipStart = range.getLower().toString();
                        ipEnd = range.getUpper().toString();
                    }
                } catch (NullPointerException | IndexOutOfBoundsException e) {
                    if (Info.gui) throw new InvalidRangeException();
                    else {
                        System.out.println("2");
                        help();
                    }
                }
            }

            try {
                if (cmd.hasOption("rangefile")) {
                    String rangeList = cmd.getOptionValue("rangefile");
                    FileReader fileReader = new FileReader(rangeList);
                    try (BufferedReader reader = new BufferedReader(fileReader)) {
                        List<String> lines = new ArrayList<>();
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            lines.add(line);
                        }
                        IpList[] ipLists = new IpList[lines.size()];
                        final int[] index = {0};
                        lines.forEach((s) -> {
                            IPAddressSeqRange range = new IPAddressString(s).getSequentialRange();
                            ipLists[index[0]] = new IpList(range.getLower().toString(), range.getUpper().toString());
                            index[0]++;
                        });
                        ipRanges = ipLists;
                    }
                } else {
                    ipList = new IpList(ipStart, ipEnd);
                    ipRanges = new IpList[]{ipList};
                }

            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                portrange = new PortList(cmd.getOptionValue("ports"));
            } catch (NumberFormatException e) {
                if (Info.gui) throw new NumberFormatException();
                System.out.println("3");
                help();
            }

            ping = !cmd.hasOption("noping");
        } catch (ParseException e) {
            help(); //help contiene system.exit
        }

        if (isOutput()) {
            filename = FileUtils.getCorrectFileName("outputs/" + ipStart + "-" + ipEnd);
            FileUtils.appendToFile("quboScanner by @zreply - Version " + Info.version + " " + Info.otherVersionInfo, filename);
        } else filename = null;
    }

    public boolean isPing() {
        return ping;
    }

    public boolean isOutput() {
        return !cmd.hasOption("nooutput");
    }

    public boolean isSkipCommonPorts() {
        return !cmd.hasOption("all");
    }

    public boolean isFulloutput() {
        return cmd.hasOption("fulloutput");
    }

    public void setPing(boolean ping) {
        this.ping = ping;
    }

    public int getCount() {
        return Integer.parseInt(cmd.getOptionValue("c", "1"));
    }

    public IpList getIpList() {
        return ipList;
    }

    public PortList getPortrange() {
        return portrange;
    }

    public int getThreads() throws NumberFormatException {
        return Integer.parseInt(cmd.getOptionValue("th"));
    }

    public int getTimeout() {
        return Integer.parseInt(cmd.getOptionValue("ti"));
    }

    public String getFilename() {
        return filename;
    }

    public String getMotd() {
        return cmd.getOptionValue("filtermotd", "");
    }

    public String getVersion() {
        return cmd.getOptionValue("filterversion", "");
    }

    public int getMinPlayer() {
        return Integer.parseInt(cmd.getOptionValue("on", "-1"));
    }

    public boolean isDebugMode() {
        return cmd.hasOption("debug");
    }

    public IpList[] getIpRanges() {
        return ipRanges;
    }

    public void setIpList(IpList ipList) {
        this.ipList = ipList;
    }
}
