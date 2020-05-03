import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Tarificate {

    static String filename = "\\data.csv"; // name of CDR file

    public static void main (String [] args) throws IOException {

        String subNumber;
        if (args.length == 0) subNumber = "";
        else subNumber = args[0];

        String path = new File("").getAbsolutePath() + filename;
        List<cdrRow> cdr = parse (path);

        calcilation(cdr, new Tarif(subNumber));
    }

    private static List<cdrRow> parse (String path) throws IOException {
        List<cdrRow> cdr = new ArrayList<>();
        List<String> fileLines = Files.readAllLines(Paths.get(path));
        fileLines.remove(0);

        for (String fileLine : fileLines) {
            String[] splitedText = fileLine.split(",");

            cdrRow current = new cdrRow (splitedText);
            cdr.add(current);
        }

        return cdr;
    }

    private static void calcilation (List<cdrRow> cdr, Tarif tarif) {
        for (cdrRow cdrrow : cdr) {
            if (cdrrow.msisdn_origin.equals(tarif.subNumber)) {
                tarif.Ti += Math.ceil(cdrrow.call_duration);
                tarif.Ts += cdrrow.sms_number;
            }
            if (cdrrow.msisdn_dest.equals(tarif.subNumber)) tarif.Tu += Math.ceil(cdrrow.call_duration);
        }

        System.out.println(tarif.countNprint());
    }
}

class cdrRow {

    String timestamp;
    String msisdn_origin;
    String msisdn_dest;
    double call_duration;
    int sms_number;

    cdrRow (String [] splitedText) {
        this.timestamp = splitedText[0];
        this.msisdn_origin = splitedText[1];
        this.msisdn_dest = splitedText[2];
        this.call_duration = Double.parseDouble(splitedText[3]);
        this.sms_number = Integer.parseInt(splitedText[4]);
    }
}

class Tarif {
    String subNumber;

    int ki = 4, ku = 1, ks = 1; // price incoming minutes/upcoming minutes/sms
    int fi = 0, fu = 5, fs = 5; // free incoming minutes/upcoming minutes/sms

    int Ti, Tu, Ts;
    int It, Ut, St, S;

    Tarif (String subNumber) {
        if (subNumber.length() == 0) this.subNumber = "968247916";
        else this.subNumber = subNumber;
    }

    void count() {
        It = Math.max((Ti-fi), 0)*ki;
        Ut = Math.max((Tu-fu), 0)*ku;
        St = Math.max((Ts-fs), 0)*ks;
        S =  It + Ut + St;
    }

    String countNprint() {
        count();
        String printmessage = subNumber + ":\n" +
                "incoming calls:\t" + Ti + " min.\t = " + It + " rub.\n" +
                "upcoming calls:\t" + Tu + " min.\t = " + Ut + " rub.\n" +
                "sms:\t" + Ts + "\t\t = " + St + " rub.\n" +
                "TOTAL\t = " + S + " rub.";

        return printmessage;
    }
} 
