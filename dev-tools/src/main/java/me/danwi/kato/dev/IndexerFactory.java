package me.danwi.kato.dev;

import me.danwi.kato.server.KatoService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class IndexerFactory implements ApplicationContextAware {
    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public Indexer getIndexer() {
        return new Indexer(context.getBeansWithAnnotation(KatoService.class));
    }
}
