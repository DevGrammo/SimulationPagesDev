package devgrammo.siteBuilder.content.property;

import devgrammo.siteBuilder.content.Content;
import devgrammo.siteBuilder.content.ContentsByPage;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class PropertyIndex {
    private final String pageType;
    private final Map<String, List<Content>> index;
    private final String property;

    public PropertyIndex(String pageType, String property, Map<String, List<Content>> index) {
        this.index = index;
        this.property = property;
        this.pageType = pageType;
    }

    public Map<String, List<Content>> getIndex() {
        return index;
    }

    public String getProperty() {
        return property;
    }

    public static BiFunction<String, ContentsByPage, PropertyIndex> makePropertyIndexF =
            PropertyIndex::makePropertyIndex;

    public static PropertyIndex makePropertyIndex(String property, ContentsByPage contents) {

        Function<Content, List<AbstractMap.SimpleEntry<String, Content>>> expandByProperty = c -> {
            List<String> slugs = c.getPropertySlugs(property);
            if (slugs == null) {
                return null;
            } else {
                return slugs.stream()
                        .map(s -> new AbstractMap.SimpleEntry<>(s, c))
                        .collect(Collectors.toList());
            }
        };

        Map<String, List<Content>> propIndex =
                contents.getContents()
                        .stream()
                        .map(expandByProperty)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .collect(groupingBy(
                                AbstractMap.SimpleEntry::getKey,
                                mapping(AbstractMap.SimpleEntry::getValue, toList())));

        return new PropertyIndex(contents.getPageType(), property, propIndex);
    }

    public String getPageType() {
        return this.pageType;
    }
}
