import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class ImageComparer {
    private final File imageFile;
    private ArrayList<File> betterThanImages;
    private ArrayList<File> worseThanImages;

    public ImageComparer(File imageFile) {
        this.imageFile = imageFile;
        this.betterThanImages = new ArrayList<>();
        this.worseThanImages = new ArrayList<>();
    }

    public boolean isBelowTop(int top){
        return worseThanImages.size() > top;
    }

    public void addBetter(ImageComparer ic){
        this.betterThanImages.add(ic.getImageFile());
    }

    public void addWorse(ImageComparer ic){
        this.worseThanImages.add(ic.getImageFile());
    }

    public File getImageFile() {
        return imageFile;
    }

    public ArrayList<File> getBetterThanImages() {
        return betterThanImages;
    }

    public ArrayList<File> getWorseThanImages() {
        return worseThanImages;
    }
}
