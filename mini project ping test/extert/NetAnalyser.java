import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class NetAnalyser {

    public static void main(String[] args) {

        int x = 0;

        if (args.length == 0) {
            JOptionPane.showMessageDialog(null, "No number is given, the default is 10", "ERROR ", JOptionPane.ERROR_MESSAGE);
            x = 10;

        }else if(Integer.valueOf(args[0]) < 10 || Integer.valueOf(args[0]) > 20)
        {
            JOptionPane.showMessageDialog(null, "Please provide valid number (10-20)", "ERROR ", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        else{
            x = Integer.valueOf(args[0]);
        }
        JFrame jFrame = new JFrame();
        JPanel jPanel = new NetPanel(x);
        jFrame.setTitle("NetAnalyser v1.0");
        jFrame.setBounds(20,20,1200,450);
        jFrame.setContentPane(jPanel);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setVisible(true);


    }
}
class NetPanel extends JPanel {
    JButton process;
    JLabel x1,x2,x3;
    JLabel bar1,bar2,bar3;
    JTextField textField_url;
    JSpinner spinner_no;
    JTextArea textArea_center;

    private int[] array;
    //the max and min RTT
    private int max,min;
    //the interval of histogram
    private int HistogramInterval;
    //the length of each element in histogram
    private int len1,len2,len3;

    public NetPanel(int x) {
        surface(x);
    }
    //the outlook of it
    public void surface(int x) {

        setPreferredSize(new Dimension(1200, 450));
        setLayout(new BorderLayout());
        //west panel
        JPanel panel_west = new JPanel();
        add(panel_west, BorderLayout.WEST);
        panel_west.setPreferredSize(new Dimension(400, 500));
        panel_west.setLayout(null);

        JLabel label_tiltip = new JLabel("Enter Test URL & no. of probes and click on Process");
        panel_west.add(label_tiltip);
        label_tiltip.setBounds(40, 76, 330, 30);

        JLabel label_url = new JLabel("Test URL");
        panel_west.add(label_url);
        label_url.setBounds(70, 152, 130, 30);
        textField_url = new JTextField();
        panel_west.add(textField_url);
        textField_url.setBounds(150, 152, 170, 30);

        JLabel label_no = new JLabel("No. of probes");
        panel_west.add(label_no);
        label_no.setBounds(100, 228, 130, 30);
        spinner_no = new JSpinner();
        panel_west.add(spinner_no);
        spinner_no.setModel(new SpinnerNumberModel(1, 1, x, 1));
        spinner_no.setBounds(200, 228, 70, 30);

        process = new JButton("Process");
        panel_west.add(process);
        process.setBounds(140, 304, 100, 30);
        //lisener of button
        process.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                click();
            }
        });

        //the center panel
        JScrollPane panel_center = new JScrollPane();
        add(panel_center, BorderLayout.CENTER);
        panel_center.setPreferredSize(new Dimension(400, 450));
        textArea_center = new JTextArea("Your output will appear here...\n");
        panel_center.setViewportView(textArea_center);
        textArea_center.setEditable(false);

        //the east panel
        JPanel panel_east = new JPanel();
        add(panel_east, BorderLayout.EAST);
        panel_east.setPreferredSize(new Dimension(400, 450));
        panel_east.setLayout(null);

        JLabel histogram = new JLabel("Histogram");
        panel_east.add(histogram);
        histogram.setBounds(165, 76, 70, 30);

        bar1 = new JLabel("");
        panel_east.add(bar1);
        bar1.setHorizontalAlignment(SwingConstants.RIGHT);
        bar1.setBounds(20, 152, 130, 30);
        x1 = new JLabel("");
        panel_east.add(x1);
        x1.setBounds(170, 152, 250, 30);


        bar2 = new JLabel("");
        panel_east.add(bar2);
        bar2.setHorizontalAlignment(SwingConstants.RIGHT);
        bar2.setBounds(20, 228, 130, 30);
        x2 = new JLabel("");
        panel_east.add(x2);
        x2.setBounds(170, 228, 250, 30);


        bar3 = new JLabel("");
        panel_east.add(bar3);
        bar3.setHorizontalAlignment(SwingConstants.RIGHT);
        bar3.setBounds(20, 304, 130, 30);
        x3 = new JLabel("");
        panel_east.add(x3);
        x3.setBounds(170, 304, 250, 30);

    }

    Pattern pattern = Pattern.compile("=\\d+ms");

    //when click the button
    private void click() {
        //initialize everything
        textArea_center.setText("");
        bar1.setText("");
        bar2.setText("");
        bar3.setText("");
        x1.setText("");
        x2.setText("");
        x3.setText("");
        len1 = 0;
        len2 = 0;
        len3 = 0;
        Process p = null;
        String line = null;
        ArrayList<Integer> RTTarray = new ArrayList<>();

        try {
            //to test the url
            String url = textField_url.getText().trim();
            if ((url != null && !"".equals(url.trim()) && url.contains(".")) == false) {
                textArea_center.append("The test url \"" + textField_url.getText() + "\" is invalid.\n");
                return;
            }


            p = Runtime.getRuntime().exec("cmd /c ping -n " + spinner_no.getValue() + " " + textField_url.getText().trim());
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "GBK"));
            if (p.exitValue() == 0) {

                while ((line = reader.readLine()) != null) {
                    textArea_center.append(line + "\n");
                    // find RTTs
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        RTTarray.add(Integer.valueOf(matcher.group().replaceAll("[^0-9]", "")));
                    }
                }
                array = RTTarray.stream().mapToInt(Integer::valueOf).toArray();
                if (RTTarray.size() == 0) {
                    textArea_center.setText("All of the requests were timeout.\n");
                    return;
                }
                max = max(array);
                min = min(array);
                //calculate the interval of histogram and length of every element
                cal_Interval_len();
                //make the histogram
                makeHistogram();
                File();
            }else {
                textArea_center.append("Test URL: " + textField_url.getText().trim() + "\n");
                textArea_center.append("The ping command failed to execute.\n");
            }
        }

        catch (IOException ex) {
            ex.printStackTrace();
            textArea_center.setText(ex.getMessage() + "\n");
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
            textArea_center.setText(ex.getMessage() + "\n");
        }

    }


    //find the min RTT
    private int min(int[] x) {
        int min = x[0];
        for (int i = 0; i < x.length; i++) {
            min = min > x[i] ? x[i] : min;
        }
        return min;
    }
    //the max RTT
    private int max(int[] x) {
        int max = x[0];
        for (int i = 0; i < x.length; i++) {
            max = max < x[i] ? x[i] : max;
        }
        return max;
    }

    //calculate the interval of histogram and length of every element
    private void cal_Interval_len() {
        if (max - min < 3)
            HistogramInterval = 1;
        else
            HistogramInterval = (int) Math.ceil((max - min) / 3.0);


        for (int i = 0; i < array.length; i++) {
            if (array[i] >= min && array[i] < min + HistogramInterval)
                len1++;
            else if (array[i] < min + 2 * HistogramInterval&&array[i] >= min + HistogramInterval )
                len2++;
            else if (array[i] <= min + 3 * HistogramInterval&&array[i] >= min + 2 * HistogramInterval)
                len3++;

        }
    }

    //make the histogram
    private void makeHistogram() {
        bar1.setText(min + "<=RTT<" + (min + HistogramInterval));
        bar2.setText((min + HistogramInterval) + "<=RTT<" + (min + 2 * HistogramInterval));
        bar3.setText((min + 2 * HistogramInterval) + "<=RTT<=" + (min + 3 * HistogramInterval));
        paintX(x1,len1);
        paintX(x2,len2);
        paintX(x3,len3);

    }

    //paint the x on the label
    private void paintX(JLabel as,int len) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            sb.append("X ");
        }
        as.setText(sb.toString());
    }
    //write the file
    private void File() throws FileNotFoundException {

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        LocalDateTime time = LocalDateTime.now();
        String file = textField_url.getText().trim().replace(".", "-") + "-" + format.format(time) + ".txt";
        PrintWriter printWriter = new PrintWriter(file);
        printWriter.println(file);
        printWriter.println();
        printWriter.println("RTT(ms) histogram");
        printWriter.println(min + "-" + (min + HistogramInterval) + ": " + len1);
        printWriter.println((min + HistogramInterval) + "-" + (min + 2 * HistogramInterval) + ": " + len2);
        printWriter.println((min + 2 * HistogramInterval) + "-" + (min + 3 * HistogramInterval) + ": " + len3);
        printWriter.close();
    }
}






