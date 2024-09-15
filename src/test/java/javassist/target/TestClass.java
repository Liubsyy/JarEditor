package javassist.target;

/**
 * jar内新增此class，然后用javassist进行增删改测试 内部类和字段重名问题和内部类修改问题
 * @author Liubsyy
 * @date 2024/9/15
 */
public class TestClass {
    private static String staticName = "TestClass";
    private String name;
    private int value=100;
    private int value1;
    private int value2;

    static {
        System.out.println("TestClass static...");
    }

    public TestClass() {
        name = "Tom";
    }

    public TestClass(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public void setValue1(int value1) {
        this.value1 = value1;
    }

    public void setValue2(int value2) {
        this.value2 = value2;
    }

    static class InnerClass1{
        private static String staticName = "InnerClass1";
        private String name;
        private int value=1;
        private int innerClass1_value1;
        private int innerClass1_value2;

        public InnerClass1(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public void setInnerClass1_value1(int innerClass1_value1) {
            this.innerClass1_value1 = innerClass1_value1;
        }

        public void setInnerClass1_value2(int innerClass1_value2) {
            this.innerClass1_value2 = innerClass1_value2;
        }

        static class InnerClass11{
            private static String staticName = "InnerClass11";
            private String name;
            private int value=11;

            public InnerClass11(String name, int value) {
                this.name = name;
                this.value = value;
            }
        }
        static class InnerClass12{
            private static String staticName = "InnerClass12";
            private int value=12;
            private String name;
        }
        static class InnerClass13{
            private static String staticName = "InnerClass13";
            private int value=13;
            private String name;
        }

    }

    static class InnerClass2{
        private static String staticName = "InnerClass2";
        private String name;
        private int value=2;
        private int innerClass2_value1;
        private int innerClass2_value2;

        public InnerClass2(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public int getInnerClass2_value1() {
            return innerClass2_value1;
        }

        public int getInnerClass2_value2() {
            return innerClass2_value2;
        }
    }

    static class InnerClass3{
        private static String staticName = "InnerClass3";
        private String name;
        private int value=3;
        private int innerClass3_value1;
        private int innerClass3_value2;

        public InnerClass3(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public void setInnerClass3_value1(int innerClass3_value1) {
            this.innerClass3_value1 = innerClass3_value1;
        }

        public void setInnerClass3_value2(int innerClass3_value2) {
            this.innerClass3_value2 = innerClass3_value2;
        }

        static class InnerClass31{
            private static String staticName = "InnerClass31";
            private String name;
            private int value=31;
            private int innerClass31_value1;

            public InnerClass31(String name, int value) {
                this.name = name;
                this.value = value;
            }

            public void setInnerClass31_value1(int innerClass31_value1) {
                this.innerClass31_value1 = innerClass31_value1;
            }
        }
        static class InnerClass32{
            private static String staticName = "InnerClass32";
            private int value=32;
            private String name;
            private int innerClass32_value1;

            public void setInnerClass32_value1(int innerClass32_value1) {
                this.innerClass32_value1 = innerClass32_value1;
            }
        }
        static class InnerClass33{
            private static String staticName = "InnerClass33";
            private int value=33;
            private String name;
            private int innerClass33_value1;

            public void setInnerClass33_value1(int innerClass33_value1) {
                this.innerClass33_value1 = innerClass33_value1;
            }
        }

    }

}
