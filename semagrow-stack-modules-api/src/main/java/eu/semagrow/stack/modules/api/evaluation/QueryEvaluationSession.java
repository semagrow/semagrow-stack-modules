package eu.semagrow.stack.modules.api.evaluation;


/**
 * Created by angel on 6/11/14.
 */
public interface QueryEvaluationSession {

    SessionId getSessionId();

    EvaluationStrategy getEvaluationStrategy();

}
