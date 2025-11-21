package com.example.demo.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.type.AnnotationMetadata;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class EnvironmentConfig implements EnvironmentPostProcessor, ImportBeanDefinitionRegistrar {

    private static final List<String> OUTPUT_BINDINS = new LinkedList<>();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        final var functionalBindings = createFunctionalBindings(environment);
        var inputBindings = new ArrayList<String>();

        for (var binding : functionalBindings) {
            if (binding.contains("-in-") && !inputBindings.contains(binding)) {
                inputBindings.add(binding);
            }
        }

        for (var binding : functionalBindings) {
            if (binding.contains("-out-")
                    && inputBindings.stream().map(b -> b.replaceAll("-in-\\d+", ""))
                    .noneMatch(b -> b.equals(binding.replaceAll("-out-\\d+", "")))) {

                OUTPUT_BINDINS.add(binding);
            }
        }

        var functionSource = new HashMap<String, Object>(Map.of(
                "spring.cloud.function.definition", inputBindings.stream().map(it -> it.replaceAll("-in-\\d+", "")).distinct().collect(Collectors.joining(";"))
        ));

        environment.getPropertySources().addFirst(new MapPropertySource("Kafka functional bindings", functionSource));
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        if (registry instanceof ConfigurableBeanFactory factory) {
            Map<String, List<AbstractMap.SimpleEntry<String, String>>> collect = OUTPUT_BINDINS.stream()
                    .map(it -> new AbstractMap.SimpleEntry<>(it.replaceAll("-out-\\d+", ""), it))
                    .filter(it -> !registry.isBeanNameInUse(it.getKey()))
                    .collect(Collectors.groupingBy(AbstractMap.SimpleEntry::getKey));

            collect.entrySet()
                    .stream()
                    .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), (Supplier<OutputBridge>) () -> {
                        var streamBridge = factory.getBean(StreamBridge.class);
                        return new OutputBridge(streamBridge) {
                            @Override
                            public List<String> channels() {
                                return entry.getValue().stream().map(AbstractMap.SimpleEntry::getValue).toList();
                            }
                        };
                    }))
                    .map(this::createBeanDefinition)
                    .forEach(it -> registry.registerBeanDefinition(it.getKey(), it.getValue()));
        }
    }

    private Map.Entry<String, BeanDefinition> createBeanDefinition(Map.Entry<String, Supplier<OutputBridge>> binding) {
        var beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(OutputBridge.class);
        beanDefinition.setInstanceSupplier(() -> binding.getValue().get());
        return new AbstractMap.SimpleEntry<>(binding.getKey(), beanDefinition);
    }

    private Collection<String> createFunctionalBindings(ConfigurableEnvironment environment) {
        return environment.getPropertySources()
                .stream()
                .filter(MapPropertySource.class::isInstance)
                .map(MapPropertySource.class::cast)
                .map(MapPropertySource::getPropertyNames)
                .flatMap(Arrays::stream)
                .filter(name -> name.startsWith("spring.cloud.stream.bindings.") && name.endsWith(".destination"))
                .map(name -> name.replace("spring.cloud.stream.bindings.", "").replaceAll("\\.\\w+", ""))
                .toList();
    }
}
