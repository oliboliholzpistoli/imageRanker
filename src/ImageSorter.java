import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

//TODO Comment things
public class ImageSorter {

        int choice = -1;
        int squareSize = 1000;
        JPanel currentPanel = null;
        JFrame frame = new JFrame();

        private JPanel panelCreator(File first, File second) throws IOException {
            JPanel p = new JPanel();
            p.setLayout(new BorderLayout());

            BufferedImage biFirst = ImageIO.read(first);
            System.out.println("Width1: "+biFirst.getWidth());
            System.out.println("Height1: " + biFirst.getHeight());
            JButton bFirst = new JButton(new ImageIcon(resizeImage(biFirst)));
            bFirst.setBorder(BorderFactory.createEmptyBorder());
            bFirst.setContentAreaFilled(false);
            bFirst.setPreferredSize(new Dimension(squareSize, squareSize));
            bFirst.addActionListener(e -> choice = 0);

            BufferedImage biSecond = ImageIO.read(second);
            System.out.println("Width2: "+biSecond.getWidth());
            System.out.println("Height2: " + biSecond.getHeight());
            JButton bSecond = new JButton(new ImageIcon(resizeImage(biSecond)));
            bSecond.setBorder(BorderFactory.createEmptyBorder());
            bSecond.setContentAreaFilled(false);
            bSecond.setPreferredSize(new Dimension(squareSize, squareSize));
            bSecond.addActionListener(e -> choice = 1);

            p.add(bFirst, BorderLayout.WEST);
            p.add(bSecond,BorderLayout.EAST);
            return p;
        }

        private int[] calculateImageSize (int width,int height){
            int[] output = new int[2];
            if (width > height){
                output[0] = squareSize;
                output[1] = (int)(((double)height/(double)width)* squareSize);
            } else {
                output[0] = (int)(((double)width/(double)height)* squareSize);
                output[1] = squareSize;
            }

            return output;
        }

        private BufferedImage resizeImage(BufferedImage bi){
            int[] calculatedSizes = calculateImageSize(bi.getWidth(),bi.getHeight());
            BufferedImage output = new BufferedImage(calculatedSizes[0],calculatedSizes[1],bi.getType());
            Graphics2D g2d = output.createGraphics();
            g2d.drawImage(bi,0,0,calculatedSizes[0],calculatedSizes[1],null);
            g2d.dispose();
            return output;
        }

        private int showChoice(File first, File second) throws IOException, InterruptedException {
            if (currentPanel != null) {
                frame.remove(currentPanel);
            }
            JPanel newPanel = panelCreator(first, second);
            currentPanel = newPanel;
            frame.add(newPanel);
            frame.setVisible(true);

            while(choice == -1) {
                Thread.sleep(100);
            }

            int output = choice;
            choice = -1;
            return  output;
        }

        private ArrayList<File> mergeSort(ArrayList<File> files) throws IOException, InterruptedException {
            ArrayList<File> first = new ArrayList<>(files.subList(0,files.size()/2));
            ArrayList<File> second = new ArrayList<>(files.subList(files.size()/2,files.size()));
            ArrayList<File> output = new ArrayList<>();
            if(first.size() > 1){
                first = mergeSort(first);
            }
            if (second.size() > 1){
                second = mergeSort(second);
            }
            int i = 0;
            int e = 0;
            int choice;
            do {
                if (i == first.size()){
                    output.add(second.get(e));
                    e++;
                } else if (e == second.size()){
                    output.add(first.get(i));
                    i++;
                } else {
                    choice = showChoice(first.get(i), second.get(e));
                    if (choice == 0){
                        output.add(second.get(e));
                        e++;
                    } else {
                        output.add(first.get(i));
                        i++;
                    }
                }
            } while (i < first.size() || e < second.size());
            System.out.println(files.size());
            System.out.println(first.size());
            System.out.println(second.size());
            return output;
        }

        public void run() {
            try {
                JFrame f = new JFrame();
                frame.setLayout(new BorderLayout());
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

                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(squareSize * 2, squareSize);

                ArrayList<File> fileList = new ArrayList<>(Arrays.asList(images));

                int maxTop = Math.min(fileList.size(), 10);

                //Merge Sort
                ArrayList<File> sortedList = mergeSort(fileList);

                ArrayList<File> topImages = new ArrayList<>(new ArrayList<>(sortedList.subList(0, sortedList.size())));
                frame.remove(currentPanel);
                frame.setLayout(new BorderLayout());
                JLabel l = new JLabel(new ImageIcon(resizeImage(ImageIO.read(sortedList.get(sortedList.size()-1)))));
                frame.add(l);
                frame.setVisible(true);
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