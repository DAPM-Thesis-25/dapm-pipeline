package repository;

import org.springframework.stereotype.Repository;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.ProcessingElement;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Repository
public class TemplateRepository {
    private final Map<String, Class<? extends ProcessingElement>> templates = new HashMap<>();

    public void storeTemplate(String templateID, Class<? extends ProcessingElement> template) {
        if (templates.containsKey(templateID)) { throw new IllegalArgumentException("Template ID already existsss: " + templateID); }
        templates.put(templateID, template);
    }

    public <T extends ProcessingElement> T createInstanceFromTemplate(String templateID, Configuration configuration) {
        Class<? extends ProcessingElement> template = templates.get(templateID);
        if (template == null) { throw new RuntimeException("No template found for template ID: " + templateID); }
        try {
            return (T) template.getDeclaredConstructor(Configuration.class).newInstance(configuration);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Class<? extends ProcessingElement>> getTemplates() {
        return templates;
    }
}
