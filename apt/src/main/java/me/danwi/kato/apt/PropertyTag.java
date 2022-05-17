package me.danwi.kato.apt;

import com.github.chhorz.javadoc.tags.StructuredTag;

public class PropertyTag extends StructuredTag {
    private static final String TAG_NAME = "property";

    private static final String PROPERTY_NAME = "propertyName";
    private static final String DESCRIPTION = "description";

    public PropertyTag() {
        super(TAG_NAME, new Segment(PROPERTY_NAME), new Segment(DESCRIPTION, false));
    }

    public String getPropertyName() {
        return getValues().get(PROPERTY_NAME);
    }

    public String getDescription() {
        return getValues().get(DESCRIPTION);
    }
}
