import org.apache.commons.io.FileUtils;

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

//TODO Comment things
public class ImageSorter {

    final int TOP_NUMBER = 10;
    int choiceCounter = 0;
    int choice = -1;
    int squareSize = 1000;
    JPanel currentPanel = null;
    JFrame frame = new JFrame();


    public ImageSorter() {
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(squareSize * 2, squareSize);
    }

    private JPanel panelCreator(File first, File second) throws IOException {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        BufferedImage biFirst = ImageIO.read(first);
        System.out.println("Width1: " + biFirst.getWidth());
        System.out.println("Height1: " + biFirst.getHeight());
        JButton bFirst = new JButton(new ImageIcon(resizeImage(biFirst)));
        bFirst.setBorder(BorderFactory.createEmptyBorder());
        bFirst.setContentAreaFilled(false);
        bFirst.setPreferredSize(new Dimension(squareSize, squareSize));
        bFirst.addActionListener(e -> choice = 0);

        BufferedImage biSecond = ImageIO.read(second);
        System.out.println("Width2: " + biSecond.getWidth());
        System.out.println("Height2: " + biSecond.getHeight());
        JButton bSecond = new JButton(new ImageIcon(resizeImage(biSecond)));
        bSecond.setBorder(BorderFactory.createEmptyBorder());
        bSecond.setContentAreaFilled(false);
        bSecond.setPreferredSize(new Dimension(squareSize, squareSize));
        bSecond.addActionListener(e -> choice = 1);

        p.add(bFirst, BorderLayout.WEST);
        p.add(bSecond, BorderLayout.EAST);
        return p;
    }

    private int[] calculateImageSize(int width, int height) {
        int[] output = new int[2];
        if (width > height) {
            output[0] = squareSize;
            output[1] = (int) (((double) height / (double) width) * squareSize);
        } else {
            output[0] = (int) (((double) width / (double) height) * squareSize);
            output[1] = squareSize;
        }

        return output;
    }

    private BufferedImage resizeImage(BufferedImage bi) {
        int[] calculatedSizes = calculateImageSize(bi.getWidth(), bi.getHeight());
        BufferedImage output = new BufferedImage(calculatedSizes[0], calculatedSizes[1], bi.getType());
        Graphics2D g2d = output.createGraphics();
        g2d.drawImage(bi, 0, 0, calculatedSizes[0], calculatedSizes[1], null);
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

        while (choice == -1) {
            Thread.sleep(100);
        }

        int output = choice;
        choice = -1;
        choiceCounter++;
        return output;
    }

    private ArrayList<ImageComparer> mergeSort(ArrayList<ImageComparer> images) throws IOException, InterruptedException {
        ArrayList<ImageComparer> first = new ArrayList<>(images.subList(0, images.size() / 2));
        ArrayList<ImageComparer> second = new ArrayList<>(images.subList(images.size() / 2, images.size()));
        ArrayList<ImageComparer> output = new ArrayList<>();
        if (first.size() > 1) {
            first = mergeSort(first);
        }
        if (second.size() > 1) {
            second = mergeSort(second);
        }
        int i = 0;
        int e = 0;
        int choice;
        do {
            if (i == first.size()) {
                output.add(second.get(e));
                e++;
            } else if (e == second.size()) {
                output.add(first.get(i));
                i++;
            } else {
                choice = showChoice(first.get(i).getImageFile(), second.get(e).getImageFile());
                if (choice == 0) {
                    output.add(second.get(e));

                    first.get(i).addBetter(second.get(e));
                    second.get(e).addWorse(first.get(i));

                    if (second.get(e).getWorseThanImages().size() > TOP_NUMBER){
                        output.remove(second.get(e));
                        ArrayList<ImageComparer> outputCopy = new ArrayList<>(output);
                        for (ImageComparer im:outputCopy){
                            if (second.get(e).getBetterThanImages().contains(im.getImageFile()))
                                output.remove(im);
                        }
                    }


                    e++;
                } else {
                    output.add(first.get(i));

                    first.get(i).addWorse(second.get(e));
                    second.get(e).addBetter(first.get(i));

                    if (first.get(i).getWorseThanImages().size() > TOP_NUMBER){
                        output.remove(first.get(i));
                        ArrayList<ImageComparer> outputCopy = new ArrayList<>(output);
                        for (ImageComparer im:outputCopy){
                            if (first.get(i).getBetterThanImages().contains(im.getImageFile()))
                                output.remove(im);
                        }
                    }

                    i++;
                }
            }
        } while (i < first.size() || e < second.size());
        System.out.println(images.size());
        System.out.println(first.size());
        System.out.println(second.size());
        return output;
    }

    public void run() {
        try {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fc.showOpenDialog(frame);
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

            ArrayList<ImageComparer> fileList = new ArrayList<>();
            for (File image : images) {
                fileList.add(new ImageComparer(image));
            }
            
            int maxTop = Math.min(fileList.size(), TOP_NUMBER);

            //Merge Sort
            ArrayList<ImageComparer> sortedList = mergeSort(fileList);

            JLabel l = new JLabel(new ImageIcon(resizeImage(ImageIO.read(sortedList.get(sortedList.size() - 1).getImageFile()))));
            frame.remove(currentPanel);
            frame.setLayout(new BorderLayout());
            frame.add(l);
            frame.setVisible(true);

            File topDir = new File("/top" + maxTop);
            topDir.mkdirs();
            FileUtils.cleanDirectory(topDir);

            ArrayList<ImageComparer> topImages = new ArrayList<>(sortedList.subList(sortedList.size() - maxTop, sortedList.size()));
            int fileCount = maxTop;
            for (int i = 0; i < maxTop; i++) {
                File topFile = topImages.get(i).getImageFile();
                Files.copy(topImages.get(i).getImageFile().toPath(), Paths.get("/top" + maxTop + "/" + fileCount + "." + topFile.getAbsolutePath().split("\\.")[1]).toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
                fileCount--;
            }

            System.out.println("Choices: " + choiceCounter);
            System.out.println("Done!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
