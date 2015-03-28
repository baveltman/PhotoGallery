package apps.baveltman.photogallery;

/**
 * Class to encapsulate photo info from Flickr
 */
public class GalleryItem {

    private String mCaption;
    private String mId;
    private String mUrl;
    private String mOwner;

    public String toString() {
        return mCaption;
    }

    public void setCaption(String mCaption) {
        this.mCaption = mCaption;
    }

    public String getCaption(){
        return mCaption;
    }

    public void setId(String id){
        mId = id;
    }

    public String getId(){
        return mId;
    }

    public void setUrl(String url){
        mUrl = url;
    }

    public String getUrl(){
        return mUrl;
    }

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }

    /**
     * @return page url for a particular GalleryItem
     */
    public String getPhotoPageUrl() {
        return "http://www.flickr.com/photos/" + mOwner + "/" + mId;
    }
}
