package space.jxz.mybatis.generator;

/**
 * @Author Create by jxz
 * @Date 2018/7/23
 */
public class PluginUtils {

    /**
     * 特殊字符变驼峰, 但保留其他字符原本的大小写.
     * 比如 "xxx_userName" -> "xxxUserName"
     */
    public static String getCamelCaseStringReserveCase(String inputString,
                                            boolean firstCharacterUppercase) {
        StringBuilder sb = new StringBuilder();

        boolean nextUpperCase = false;
        for (int i = 0; i < inputString.length(); i++) {
            char c = inputString.charAt(i);

            switch (c) {
                case '_':
                case '-':
                case '@':
                case '$':
                case '#':
                case ' ':
                case '/':
                case '&':
                    if (sb.length() > 0) {
                        nextUpperCase = true;
                    }
                    break;

                default:
                    if (nextUpperCase) {
                        sb.append(Character.toUpperCase(c));
                        nextUpperCase = false;
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }

        if (firstCharacterUppercase) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }

        return sb.toString();
    }

}
