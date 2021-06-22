package animals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class Database {
    private String filename;
    private String type;
    private ObjectMapper objectMapper;
    private String language;


    public Database(String type) {
        this.type = type;
        setObjectMapper();
    }

    private void setObjectMapper() {
        StringBuilder stringBuilder = new StringBuilder("animals");
        Locale locale = new Locale("eo");
        if (Locale.getDefault().getLanguage().equals(locale.getLanguage())) {
            stringBuilder.append("_").append(locale.getLanguage());
        }
        switch (type){
            case "xml":
                objectMapper = new XmlMapper();
                stringBuilder.append(".xml");
                break;
            case "yaml":
                objectMapper = new YAMLMapper();
                stringBuilder.append(".yaml");
                break;
            default:
                objectMapper = new JsonMapper();
                stringBuilder.append(".json");
        }
        filename=stringBuilder.toString();
    }

    public String getFilename() {
        return filename;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Node readFromFile(){
        try {
            return objectMapper.readValue(new File(filename) , Node.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveToFile(Node root){
        try {
            objectMapper.writeValue(new File(filename) , root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
