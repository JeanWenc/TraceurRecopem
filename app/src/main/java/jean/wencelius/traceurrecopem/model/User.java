package jean.wencelius.traceurrecopem.model;

/**
 * Created by Jean Wencélius on 08/04/2020.
 */
public class User {
    private String mFisherName;
    private String mFisherId;

    public String getFisherName() {
        return mFisherName;
    }

    public void setFisherName(String fisherName) {
        mFisherName = fisherName;
    }

    public String getFisherId() {
        return mFisherId;
    }

    public void setFisherId(String fisherId) {
        mFisherId = fisherId;
    }

    @Override
    public String toString() {
        return "User{" +
                "mFisherName='" + mFisherName + '\'' +
                '}';
    }
}
