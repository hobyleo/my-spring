package com.hoby.spring;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hoby
 * @since 2023-12-15
 */
public class ApplicationContext {

    private Class<?> configClass;
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
    private Map<String, Object> singletonObjects = new HashMap<>();
    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    public ApplicationContext(Class<?> configClass) {
        if (configClass == null) {
            throw new IllegalArgumentException("config class cannot be null");
        }
        this.configClass = configClass;
        scan(configClass);

        for (Map.Entry<String, BeanDefinition> entry : this.beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();

            if ("singleton".equals(beanDefinition.getScope())) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    public Object getBean(String beanName) {
        if (!beanDefinitionMap.containsKey(beanName)) {
            return null;
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if ("singleton".equals(beanDefinition.getScope())) {
            // 单例
            Object singletonBean = singletonObjects.get(beanName);
            if (singletonBean == null) {
                singletonBean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, singletonBean);
            }
            return singletonBean;
        } else {
            // 多例
            return createBean(beanName, beanDefinition);
        }
    }

    private void scan(Class<?> clazz) {
        // @ComponentScan 扫描
        if (clazz.isAnnotationPresent(ComponentScan.class)) {
            ClassLoader classLoader = clazz.getClassLoader();
            ComponentScan componentScan = clazz.getAnnotation(ComponentScan.class);
            String scanBasePackage = componentScan.value();
            if (StrUtil.isBlank(scanBasePackage)) {
                // 默认为类的当前路径
                scanBasePackage = clazz.getPackage().getName();
            }

            // 根据包路径，找到class文件绝对路径
            String packagePath = scanBasePackage.replace(".", "/");
            // class文件根路径
            File basePath = new File(classLoader.getResource("").getPath());
            // 包路径的绝对路径
            File file = new File(classLoader.getResource(packagePath).getPath());

            // 如果是文件夹，则扫描文件夹下的所有子文件
            if (file.isDirectory()) {
                // 列出所有子文件，包括子文件夹下的
                List<File> files = FileUtil.loopFiles(file);
                for (File f : files) {
                    // 拿到每个class文件的全路径
                    String reference = StrUtil.subBetween(f.getAbsolutePath(),
                                    basePath.getAbsolutePath() + File.separator, ".class")
                            .replace(File.separator, ".");
                    Class<?> loadClass;
                    try {
                        // 加载该class文件
                        loadClass = classLoader.loadClass(reference);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    // 如果class相同，则跳过
                    if (loadClass == clazz) {
                        continue;
                    }
                    // 递归扫描
                    scan(loadClass);
                }
            }
        } else if (clazz.isAnnotationPresent(Component.class)) {
            // BeanPostProcessor
            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                try {
                    BeanPostProcessor beanPostProcessor = (BeanPostProcessor) clazz.getConstructor().newInstance();
                    beanPostProcessors.add(beanPostProcessor);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            Component component = clazz.getAnnotation(Component.class);
            String beanName = component.value();
            if (StrUtil.isBlank(beanName)) {
                // 默认为首字母小写的类名
                String className = clazz.getSimpleName();
                beanName = className.substring(0, 1).toLowerCase() + className.substring(1);
            }
            String scope = "singleton";
            if (clazz.isAnnotationPresent(Scope.class)) {
                Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                if ("prototype".equals(scopeAnnotation.value())) {
                    scope = scopeAnnotation.value();
                }
            }
            // 创建bean的定义
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setType(clazz);
            beanDefinition.setScope(scope);
            beanDefinition.setLazy(false);
            beanDefinitionMap.put(beanName, beanDefinition);
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getType();
        Object instance = null;
        try {
            instance = clazz.getConstructor().newInstance();

            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                // 依赖注入
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    field.set(instance, getBean(field.getName()));
                }
            }

            if (instance instanceof BeanNameAware) {
                BeanNameAware beanNameAware = (BeanNameAware) instance;
                beanNameAware.setBeanName(beanName);
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            if (instance instanceof InitializingBean) {
                InitializingBean initializingBean = (InitializingBean) instance;
                initializingBean.afterPropertiesSet();
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

}
