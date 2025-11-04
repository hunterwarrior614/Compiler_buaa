package error;

import java.util.ArrayList;
import java.util.Comparator;

public class ErrorRecorder {
    private static final ArrayList<Error> errors = new ArrayList<>();

    public static void addError(Error error) {
        errors.add(error);
    }

    public static ArrayList<Error> getErrors() {
        errors.sort(Comparator.comparingInt(Error::getLineNumber)); // 按行号升序
        return errors;
    }

    public static boolean hasErrors() {
        return !errors.isEmpty();
    }

    public static int getErrorsCount() {
        return errors.size();
    }

    public static void resetErrors(int count) {
        // 只保留前 count 个元素
        errors.subList(count, errors.size()).clear();
    }
}
