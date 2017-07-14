package grails.plugin.mongogee;

import grails.plugin.mongogee.exception.MongoSeaChangeSetException;
import grails.util.Environment;
import org.reflections.Reflections;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.Arrays.asList;

/**
 * This is a utility class to deal with reflections and annotations.
 *
 * @author lstolowski
 * @author binle
 * @since 27/07/2014
 */
public class ChangeAgent {
    private final String changeLogsBasePackage;

    public ChangeAgent(String changeLogsBasePackage) {
        this.changeLogsBasePackage = changeLogsBasePackage;
    }

    public List<Class<?>> fetchChangeLogs() {
        Reflections reflections = new Reflections(changeLogsBasePackage);
        Set<Class<?>> changeLogs = reflections.getTypesAnnotatedWith(ChangeLog.class); // TODO remove dependency, do own method
        List<Class<?>> filteredChangeLogs = (List<Class<?>>) filterByActiveGrailsEnvironment(changeLogs);

        Collections.sort(filteredChangeLogs, new ChangeLogComparator());

        return filteredChangeLogs;
    }

    public List<Method> fetchChangeSets(final Class<?> type) throws MongoSeaChangeSetException {
        final List<Method> changeSets = filterChangeSetAnnotation(asList(type.getDeclaredMethods()));
        final List<Method> filteredChangeSets = (List<Method>) filterByActiveGrailsEnvironment(changeSets);

        Collections.sort(filteredChangeSets, new ChangeSetComparator());

        return filteredChangeSets;
    }

    public boolean isRunAlwaysChangeSet(Method changesetMethod) {
        if (changesetMethod.isAnnotationPresent(ChangeSet.class)) {
            ChangeSet annotation = changesetMethod.getAnnotation(ChangeSet.class);
            return annotation.runAlways();
        } else {
            return false;
        }
    }

    private boolean matchesActiveGrailsEnvironment(AnnotatedElement element) {
        if (element.isAnnotationPresent(ChangeEnv.class)) {
            List<String> environments = asList(element.getAnnotation(ChangeEnv.class).value());
            if (environments.contains(Environment.getCurrentEnvironment().name().toLowerCase())) {
                return true;
            } else {
                return false;
            }
        } else {
            return true; // not annotated change sets always match
        }
    }

    private List<?> filterByActiveGrailsEnvironment(Collection<? extends AnnotatedElement> annotated) {
        List<AnnotatedElement> filtered = new ArrayList<>();
        for (AnnotatedElement element : annotated) {
            if (matchesActiveGrailsEnvironment(element)) {
                filtered.add(element);
            }
        }
        return filtered;
    }

    private List<Method> filterChangeSetAnnotation(List<Method> allMethods) throws MongoSeaChangeSetException {
        final Set<String> changeSetIds = new HashSet<>();
        final List<Method> changeSetMethods = new ArrayList<>();
        for (final Method method : allMethods) {
            if (method.isAnnotationPresent(ChangeSet.class)) {
                String id = method.getAnnotation(ChangeSet.class).id();
                if (changeSetIds.contains(id)) {
                    throw new MongoSeaChangeSetException(String.format("Duplicated changeSet id found: '%s'", id));
                }
                changeSetIds.add(id);
                changeSetMethods.add(method);
            }
        }
        return changeSetMethods;
    }

}
