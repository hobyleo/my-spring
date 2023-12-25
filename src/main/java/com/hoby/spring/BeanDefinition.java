package com.hoby.spring;

/**
 * @author hoby
 * @since 2023-12-15
 */
public class BeanDefinition {

    private Class<?> type;
    private String scope;
    private boolean lazy;

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isLazy() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "type=" + type +
                ", scope='" + scope + '\'' +
                ", lazy=" + lazy +
                '}';
    }
}
