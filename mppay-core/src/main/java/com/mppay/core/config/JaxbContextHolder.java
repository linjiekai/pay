/*
 * 版权所有 (C) 2012 中国南方航空股份有限公司。
 * 本文件可能包含有南方航空公司的机密或专有信息。
 * 未经许可不得擅自公开、复制这些机密资料及其中任何部分，
 * 只可按照其使用许可协议，在南方航空公司内部使用。
 *
 * File Name: $(#)JaxbContextHolder.java
 * Creation Date: Jun 26, 2012 8:34:48 PM
 * $Id: JaxbContextHolder.java 970 2012-07-02 05:56:45Z qinx $
 */
package com.mppay.core.config;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 * JAXBContext、Marshaller和Unmarshaller实例容器工具。
 * </p>
 * 用于缓存当前虚机中用到过的上下文实例，消除相同绑定类型反复创建JAXBContext等的代价。<br>
 * 因为还没用到Schema校验，当前还不支持关联到Schema的缓存。相关参考：<a
 * href="http://robaustin.wikidot.com/how-to-improve-perforamance-of-jaxb">How To
 * Improve Performance of JAXB</a>。
 * 实践之后证明Marshaller和Unmarshaller并不是线程安全的，需要为每个使用线程创建安全机制。<br>
 * 
 * @see JAXBContext
 * @see Marshaller
 * @see Unmarshaller
 * @author sin_sin
 * @version $Revision: 970 $ $Date: 2012-07-02 13:56:45 +0800 (星期一, 02 七月 2012) $
 */
public abstract class JaxbContextHolder {

    /** 真实容器Map，线程安全。为了便于处理，键和值都用Wrapper封装起来了。 */
    protected static final ConcurrentMap<JaxbContextDescriptor, JaxbContextHelpers> holder = 
    	new ConcurrentHashMap<JaxbContextDescriptor, JaxbContextHelpers>();

    /**
     * 获取一个{@link JAXBContext}实例。
     * 
     * @param classesToBeBound 需要被JAXB识别的Java类列表
     * @return 不管哪个线程曾经生成过，如果有则返回缓存的。
     * @throws JAXBException
     */
    public static JAXBContext getJaxbContext(Class<?>... classesToBeBound)
            throws JAXBException {
        JaxbContextHelpers helpers = getInstance(classesToBeBound);
        return helpers.getContext();
    }

    /**
     * 获取一个{@link Marshaller}实例。<br>
     * 注意Adaptor、EventHandler之类属性可能被定制过，如果需要可以用{@link #newMarshaller(Class...)}
     * 重新创建一个干净实例。
     * 
     * @param classesToBeBound 需要被JAXB识别的Java类列表
     * @return 如果本线程曾经生成过则返回缓存的，否则都是新实例。
     * @throws JAXBException
     */
    public static Marshaller getMarshaller(Class<?>... classesToBeBound)
            throws JAXBException {
        JaxbContextHelpers helpers = getInstance(classesToBeBound);
        Marshaller marshaller = helpers.getMarshaller();
        if (marshaller == null) {
            marshaller = helpers.getContext().createMarshaller();
            helpers.setMarshaller(marshaller);
        }
        return marshaller;
    }

    /**
     * 新生成一个{@link Marshaller}实例。
     * 
     * @param classesToBeBound 需要被JAXB识别的Java类列表
     * @return 总是返回一个新生成的，JAXBContext可能是被缓存的。
     * @throws JAXBException
     */
    public static Marshaller newMarshaller(Class<?>... classesToBeBound)
            throws JAXBException {
        JaxbContextHelpers helpers = getInstance(classesToBeBound);
        return helpers.getContext().createMarshaller();
    }

    /**
     * 获取一个{@link Unmarshaller} 实例。<br>
     * 注意Adaptor、EventHandler之类属性可能被定制过，如果需要可以用{@link #newUnmarshaller(Class...)}
     * 重新创建一个干净实例。
     * 
     * @param classesToBeBound 需要被JAXB识别的Java类列表
     * @return 如果本线程曾经生成过则返回缓存的，否则都是新实例。
     * @throws JAXBException
     */
    public static Unmarshaller getUnmarshaller(Class<?>... classesToBeBound)
            throws JAXBException {
        JaxbContextHelpers helpers = getInstance(classesToBeBound);
        Unmarshaller unmarshaller = helpers.getUnmarshaller();
        if (unmarshaller == null) {
            unmarshaller = helpers.getContext().createUnmarshaller();
            helpers.setUnmarshaller(unmarshaller);
        }
        return unmarshaller;
    }

    /**
     * 新生成一个{@link Unmarshaller}实例。
     * 
     * @param classesToBeBound 需要被JAXB识别的Java类列表
     * @return 总是返回一个新生成的，JAXBContext可能是被缓存的。
     * @throws JAXBException
     */
    public static Unmarshaller newUnmarshaller(Class<?>... classesToBeBound)
            throws JAXBException {
        JaxbContextHelpers helpers = getInstance(classesToBeBound);
        return helpers.getContext().createUnmarshaller();
    }

    /**
     * 获取一个{@link JaxbContextHelpers}容器实例。
     * 
     * @see #newInstance(Class...)
     * @param classesToBeBound 需要被JAXB识别的Java类列表
     * @return 不管哪个线程曾经生成过，如果有则返回缓存的。
     * @throws JAXBException
     */
    public static JaxbContextHelpers getInstance(Class<?>... classesToBeBound)
            throws JAXBException {
        JaxbContextHelpers helpers = holder.get(new JaxbContextDescriptor(
                classesToBeBound));
        return helpers == null ? newInstance(classesToBeBound) : helpers;
    }

    /**
     * Obtain new instances of JAXBContext and related classes. New instance will
     * be cached if there is no one in the holder.
     * 
     * @see JAXBContext#newInstance(Class...)
     * @see JAXBContext#createMarshaller()
     * @see JAXBContext#createUnmarshaller()
     * @param classesToBeBound list of java classes to be recognized by the new
     *            JAXBContext.
     * @return A new instance of a JaxbContextHelpers container. Always non-null
     *         valid object.
     * @throws JAXBException if an error was encountered while creating the
     *             JAXBContext
     */
    public static JaxbContextHelpers newInstance(Class<?>... classesToBeBound)
            throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(classesToBeBound);
        Marshaller marshaller = context.createMarshaller();
        Unmarshaller unmarshaller = context.createUnmarshaller();
        JaxbContextHelpers helpers = new JaxbContextHelpers(context,
                marshaller, unmarshaller);
        holder.putIfAbsent(new JaxbContextDescriptor(classesToBeBound), helpers);
        return helpers;
    }

    /**
     * <p>
     * JAXB上下文绑定依据描述容器。
     * </p>
     * 判断上下文是否可以重用就依赖这里实现的{@link #hashCode()}和{@link #equals(Object)}方法。
     */
    static class JaxbContextDescriptor {

        final Class<?>[] classesToBeBound;
        final String schemaLocation;

        JaxbContextDescriptor(final Class<?>... classesToBeBound) {
            this.classesToBeBound = classesToBeBound;
            this.schemaLocation = null;
        }

        JaxbContextDescriptor(final String schemaLocation,
                final Class<?>... classesToBeBound) {
            this.classesToBeBound = classesToBeBound;
            this.schemaLocation = schemaLocation;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(classesToBeBound);
            result = prime
                    * result
                    + ((schemaLocation == null) ? 0 : schemaLocation.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof JaxbContextDescriptor))
                return false;
            JaxbContextDescriptor other = (JaxbContextDescriptor) obj;
            if (!Arrays.equals(classesToBeBound, other.classesToBeBound))
                return false;
            if (schemaLocation == null) {
                if (other.schemaLocation != null)
                    return false;
            } else if (!schemaLocation.equals(other.schemaLocation))
                return false;
            return true;
        }

    }

    /**
     * <p>
     * JAXB上下文助手工具容器。
     * </p>
     * 现在里面放了{@link JAXBContext}、{@link Marshaller}和{@link Unmarshaller}。后两者线程不安全。
     */
    public static class JaxbContextHelpers {

        private JAXBContext context;
        private ThreadLocal<Marshaller> marshaller = new ThreadLocal<Marshaller>();
        private ThreadLocal<Unmarshaller> unmarshaller = new ThreadLocal<Unmarshaller>();

        /**
         * 只传入JAXBContext的构造。
         */
        public JaxbContextHelpers(JAXBContext context) {
            this.context = context;
        }

        /**
         * 传入所有内容并构造新实例。
         * 
         * @param context
         * @param marshaller
         * @param unmarshaller
         */
        public JaxbContextHelpers(JAXBContext context, Marshaller marshaller,
                Unmarshaller unmarshaller) {
            this.context = context;
            this.marshaller.set(marshaller);
            this.unmarshaller.set(unmarshaller);
        }

        /**
         * @return {@link JAXBContext}
         */
        public JAXBContext getContext() {
            return context;
        }

        void setContext(JAXBContext context) {
            this.context = context;
        }

        /**
         * @return {@link Marshaller}
         */
        public Marshaller getMarshaller() {
            return marshaller.get();
        }

        void setMarshaller(Marshaller marshaller) {
            this.marshaller.set(marshaller);
        }

        /**
         * @return {@link Unmarshaller}
         */
        public Unmarshaller getUnmarshaller() {
            return unmarshaller.get();
        }

        void setUnmarshaller(Unmarshaller unmarshaller) {
            this.unmarshaller.set(unmarshaller);
        }

    }
}