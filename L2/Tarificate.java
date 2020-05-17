import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Tarificate {

    static String filename = "\\data.csv"; // name of NF file

    public static void main (String [] args) throws IOException {

        String IP;
        if (args.length == 0) IP = "";
        else IP = args[0];

        String path = new File("").getAbsolutePath() + filename;
        List<NFRow> NF = parse (path);

        calcilation(NF, new Tarif(IP));
        makePlot(NF, IP.length() == 0 ? "192.168.250.1" : IP);
    }

    private static List<NFRow> parse (String path) throws IOException {
        List<NFRow> cdr = new ArrayList<>();
        List<String> fileLines = Files.readAllLines(Paths.get(path));
        fileLines.remove(0);
        for (int i = 0; i<3; i++) {
            fileLines.remove(fileLines.size()-1);
        }

        for (String fileLine : fileLines) {
            String[] splitedText = fileLine.split(",");

            NFRow current = new NFRow (splitedText);
            cdr.add(current);
        }

        return cdr;
    }

    private static void calcilation (List<NFRow> NF, Tarif tarif) {
        for (NFRow nfRow : NF) {
            if (nfRow.sa.equals(tarif.IP) || nfRow.da.equals(tarif.IP))  {
                tarif.tb += nfRow.ibyt;
            }
        }
        System.out.println(tarif.countNprint());
    }

    private static void makePlot (List <NFRow> NF, String IP) throws IOException {
        List<PlotString> plot = new ArrayList<>();

        int tkb = 0;
        for (NFRow nfRow : NF) {
            if (nfRow.sa.equals(IP) || nfRow.da.equals(IP))  {
                tkb += nfRow.ibyt;
                plot.add(new PlotString(nfRow.te, tkb));
            }
        }

        for (int i = 0; i < plot.size(); i++) {
            int j = 1;

            while (i+j < plot.size() && plot.get(i).date.equals(plot.get(i+j).date)) j++;
            j--;
            while (--j >= 0) plot.remove(i+j);
        }

        FileWriter writer = new FileWriter(new File("plot.txt"));
        for (PlotString plotString : plot) writer.write(plotString.date + " " + plotString.bytes + "\n");
        writer.flush();
        writer.close();
    }
}

class NFRow {

    String ts, te, sa, da;
    int ibyt;

    NFRow(String [] splitedText) {
        this.ts = splitedText[0];
        this.te = splitedText[1];
        this.sa = splitedText[3];
        this.da = splitedText[4];
        this.ibyt = Integer.parseInt(splitedText[12]);
    }
}

class Tarif {
    String IP;

    double k = 0.5;

    int tb;
    int tkb;
    double S;

    Tarif (String IP) {
        if (IP.length() == 0) this.IP = "192.168.250.1";
        else this.IP = IP;
    }

    void count() {
        tkb = (int) Math.round((double)tb/1024);
        int tmp = tkb;

        while (tmp > 0) {
            tmp -= 500;
            int kb = tmp > 0 ? 500 : 500+tmp;
            S = S + kb*k;
            k += 0.5;
        }
    }

    String countNprint() {
        count();
        String printmessage = IP + ":\n" +
                "traffic:\t = " + tkb + " Kb\n" +
                "Cost\t = " + S + " rub.";
        return printmessage;
    }
}

class PlotString {
    String date;
    int bytes;

    PlotString (String date, int bytes) {
        this.date = date;
        this.bytes = bytes;
    }
}