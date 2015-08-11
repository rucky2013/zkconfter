package com.alibaba.zkconfter.client.drm;

import com.alibaba.zkconfter.client.annotation.DRMAttribute;
import com.alibaba.zkconfter.client.annotation.DRMResource;

/**
 * Created by pinian.lpn on 2015/8/10.
 */
@DRMResource
public class DRMResourceTest {

    @DRMAttribute
    private String testString = "10";

    @DRMAttribute
    private int testInt = 20;

    @DRMAttribute
    private boolean testBoolean = true;

    public String getTestString() {
        return testString;
    }

    public void setTestString(String testString) {
        this.testString = testString;
    }

    public int getTestInt() {
        return testInt;
    }

    public void setTestInt(int testInt) {
        this.testInt = testInt;
    }

    public boolean isTestBoolean() {
        return testBoolean;
    }

    public void setTestBoolean(boolean testBoolean) {
        this.testBoolean = testBoolean;
    }
}
