package com.grsu.teacherassistant.beans.dialog;

import com.grsu.teacherassistant.utils.ApplicationUtils;
import com.grsu.teacherassistant.utils.FacesUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

import static com.grsu.teacherassistant.utils.FacesUtils.closeDialog;
import static com.grsu.teacherassistant.utils.FacesUtils.update;
import static com.grsu.teacherassistant.utils.PropertyUtils.EXAM_MARK_WEIGHT_PROPERTY_NAME;
import static com.grsu.teacherassistant.utils.PropertyUtils.LAST_NOTES_LOADING;
import static com.grsu.teacherassistant.utils.PropertyUtils.setProperty;

/**
 * @author Pavel Zaychick
 */
@ManagedBean(name = "propertiesBean")
@ViewScoped
@Data
public class PropertiesBean implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesBean.class);
    private static final String PROPERTIES_DIALOG_NAME = "propertiesDialog";

    private Double examMarkWeight;
    private Boolean isLastLessonNotesLoadingEnabled;

    public void init() {
        examMarkWeight = ApplicationUtils.examMarkWeight();
        isLastLessonNotesLoadingEnabled = ApplicationUtils.isLastNotesLoadingEnabled();
        FacesUtils.showDialog(PROPERTIES_DIALOG_NAME);
    }

    public void exit() {
        closeDialog(PROPERTIES_DIALOG_NAME);
    }

    public void save() {
        setProperty(EXAM_MARK_WEIGHT_PROPERTY_NAME, String.format("%.2f", examMarkWeight));
        setProperty(LAST_NOTES_LOADING, isLastLessonNotesLoadingEnabled.toString());
        update("views");
        exit();
    }

}
