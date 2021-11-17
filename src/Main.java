import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    static int CHOICE = -1;
    static int SQUARE_SIZE = 1000;

    private static JPanel panelCreator(File first, File second) throws IOException {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        BufferedImage biFirst = ImageIO.read(first);
        System.out.println("Width1: "+biFirst.getWidth());
        System.out.println("Height1: " + biFirst.getHeight());
        JButton bFirst = new JButton(new ImageIcon(resizeImage(biFirst)));
        bFirst.setBorder(BorderFactory.createEmptyBorder());
        bFirst.setContentAreaFilled(false);
        bFirst.setPreferredSize(new Dimension(SQUARE_SIZE, SQUARE_SIZE));
        bFirst.addActionListener(e -> CHOICE = 0);

        BufferedImage biSecond = ImageIO.read(second);
        System.out.println("Width2: "+biSecond.getWidth());
        System.out.println("Height2: " + biSecond.getHeight());
        JButton bSecond = new JButton(new ImageIcon(resizeImage(biSecond)));
        bSecond.setBorder(BorderFactory.createEmptyBorder());
        bSecond.setContentAreaFilled(false);
        bSecond.setPreferredSize(new Dimension(SQUARE_SIZE, SQUARE_SIZE));
        bSecond.addActionListener(e -> CHOICE = 1);

        p.add(bFirst, BorderLayout.WEST);
        p.add(bSecond,BorderLayout.EAST);
        return p;
    }

    private static int[] calculateImageSize (int width,int height){
        int[] output = new int[2];
        if (width > height){
            output[0] = SQUARE_SIZE;
            output[1] = (int)(((double)height/(double)width)*SQUARE_SIZE);
        } else {
            output[0] = (int)(((double)width/(double)height)*SQUARE_SIZE);
            output[1] = SQUARE_SIZE;
        }

        return output;
    }

    private static BufferedImage resizeImage(BufferedImage bi){
        int[] calculatedSizes = calculateImageSize(bi.getWidth(),bi.getHeight());
        BufferedImage output = new BufferedImage(calculatedSizes[0],calculatedSizes[1],bi.getType());
        Graphics2D g2d = output.createGraphics();
        g2d.drawImage(bi,0,0,calculatedSizes[0],calculatedSizes[1],null);
        g2d.dispose();
        return output;
    }

    public static void main(String[] args) {
        try {
            JFrame f = new JFrame();
            f.setLayout(new BorderLayout());
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setCurrentDirectory(new File("D:Libraries/Pictures/"));
            int option = fc.showOpenDialog(f);
            File imageFolderPath = null;
            if (option == JFileChooser.APPROVE_OPTION) {
                imageFolderPath = fc.getSelectedFile();
            } else {
                System.exit(0);
            }
            String[] validFileEndings = {"jpeg", "jpg", "png", "gif", "bmp"};
            FilenameFilter filter = (dir, name) -> {
                boolean valid = false;
                for (String s : validFileEndings) {
                    if (name.endsWith(s)) {
                        valid = true;
                        break;
                    }
                }
                return valid;
            };
            File[] images = imageFolderPath.listFiles(filter);

            if (images == null || images.length < 1) {
                System.out.println("Folder doesn't contain any images");
                System.exit(0);
            }

            System.out.println("Imagecount: " + images.length);

            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(SQUARE_SIZE * 2, SQUARE_SIZE);

            ArrayList<File> thisRound = new ArrayList<>(Arrays.asList(images));
            ArrayList<File> nextRound = new ArrayList<>();

            File first;
            File second;

            JPanel oldPanel;
            JPanel newPanel = null;
            int roundCount = 1;

            ArrayList<File> topImages = new ArrayList<>();
            int maxTop = Math.min(thisRound.size(), 10);
            int absoluteIndex = 0;
            int z = thisRound.size();
            do {
                absoluteIndex+=z/2;
                z = z/2 + z%2;
                System.out.println(z);
            }while(z!=1);

            do {
                System.out.println("########## Round NR:"+ roundCount++ + "##########");
                for (int i = 0; i < thisRound.size(); i++) {
                    first = thisRound.get(i);
                    System.out.println("First Image: " + first.getPath());
                    i++;
                    if (i < thisRound.size()) {
                        second = thisRound.get(i);
                        System.out.println("Second Image: " + second.getPath());
                    } else {
                        nextRound.add(first);
                        break;
                    }
                    oldPanel = newPanel;
                    newPanel = panelCreator(first, second);
                    if (oldPanel != null) {
                        f.remove(oldPanel);
                    }
                    f.add(newPanel);
                    f.setVisible(true);
                    while(CHOICE == -1) {
                        Thread.sleep(100);
                    }
                    if (CHOICE == 0){
                        nextRound.add(first);
                    } else {
                        nextRound.add(second);
                    }
                    if (absoluteIndex <= maxTop){
                        topImages.add(CHOICE==0?second:first);
                    }
                    absoluteIndex--;
                    CHOICE = -1;
                }
                thisRound = new ArrayList<>(nextRound);
                nextRound.clear();
            } while (thisRound.size() > 1);
            topImages.add(thisRound.get(0));
            f.remove(newPanel);
            f.setLayout(new BorderLayout());
            JLabel l = new JLabel(new ImageIcon(resizeImage(ImageIO.read(thisRound.get(0)))));
            f.add(l);
            f.setVisible(true);
            new File("/top"+maxTop).mkdirs();

            int fileCount = maxTop;
            for (int i = 0;i <= maxTop;i++){
                File topFile = topImages.get(i);
                Files.copy(topImages.get(i).toPath(), Paths.get("/top"+maxTop+"/"+fileCount+"."+topFile.getAbsolutePath().split("\\.")[1]).toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
                fileCount--;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
