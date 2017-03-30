package bit.facetracker.model;

import java.io.Serializable;

/**
 * Created by blade on 30/03/2017.
 */

public class FaceDetectResult extends BaseModel {
    public Result result;
    public class Result implements Serializable {
        public FaceModel face;
        public Fashion fashion;
    }
}
