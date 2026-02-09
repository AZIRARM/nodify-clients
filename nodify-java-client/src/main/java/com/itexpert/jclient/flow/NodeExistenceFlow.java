package com.itexpert.jclient.flow;

import reactor.core.publisher.Mono;

import java.util.function.Consumer;

import com.itexpert.jclient.http.NodifyWebClient;
import com.itexpert.jclient.models.Node;

public class NodeExistenceFlow {

    private final String nodeCode;
    private final NodifyWebClient client;

    private Consumer<Node> ifExistsHandler;
    private Runnable ifNotExistsHandler;

    private Node workingNode;

    public NodeExistenceFlow(String nodeCode, NodifyWebClient client) {
        this.nodeCode = nodeCode;
        this.client = client;
    }

    public NodeExistenceFlow ifExists(Consumer<Node> handler) {
        this.ifExistsHandler = handler;
        return this;
    }

    public NodeExistenceFlow ifNotExists(Runnable handler) {
        this.ifNotExistsHandler = handler;
        return this;
    }

    public NodeExistenceFlow save() {

        Mono<Node> flow = client.getNodeByCode(nodeCode)
                .flatMap(node -> {
                    workingNode = node;

                    if (ifExistsHandler != null) {
                        ifExistsHandler.accept(node);
                    }

                    return client.saveNode(node);
                })
                .switchIfEmpty(
                        Mono.defer(() -> {
                            if (ifNotExistsHandler != null) {
                                ifNotExistsHandler.run();
                            }

                            Node newNode = new Node();
                            newNode.setCode(nodeCode);
                            workingNode = newNode;

                            return client.saveNode(newNode);
                        })
                );

        flow.block(); // ou retourner Mono si full reactive

        return this;
    }

    public void publish() {
        if (workingNode == null) {
            throw new IllegalStateException("save() must be called before publish()");
        }

        client.publishNode(workingNode.getId())
                .block();
    }
}

