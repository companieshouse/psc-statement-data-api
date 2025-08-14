package uk.gov.companieshouse.pscstatementdataapi.services;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.companieshouse.pscstatementdataapi.PSCStatementDataApiApplication.NAMESPACE;
import static uk.gov.companieshouse.pscstatementdataapi.util.DateTimeUtil.isDeltaStale;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.exemptions.CompanyExemptions;
import uk.gov.companieshouse.api.metrics.MetricsApi;
import uk.gov.companieshouse.api.metrics.RegisterApi;
import uk.gov.companieshouse.api.metrics.RegistersApi;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.StatementLinksType;
import uk.gov.companieshouse.api.psc.StatementList;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscstatementdataapi.api.ChsKafkaApiService;
import uk.gov.companieshouse.pscstatementdataapi.api.CompanyExemptionsApiService;
import uk.gov.companieshouse.pscstatementdataapi.api.CompanyMetricsApiService;
import uk.gov.companieshouse.pscstatementdataapi.exception.BadRequestException;
import uk.gov.companieshouse.pscstatementdataapi.exception.ConflictException;
import uk.gov.companieshouse.pscstatementdataapi.exception.ResourceNotFoundException;
import uk.gov.companieshouse.pscstatementdataapi.exception.ServiceUnavailableException;
import uk.gov.companieshouse.pscstatementdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.pscstatementdataapi.model.Created;
import uk.gov.companieshouse.pscstatementdataapi.model.PscStatementDocument;
import uk.gov.companieshouse.pscstatementdataapi.model.ResourceChangedRequest;
import uk.gov.companieshouse.pscstatementdataapi.repository.PscStatementRepository;
import uk.gov.companieshouse.pscstatementdataapi.transform.PscStatementTransformer;

@Service
public class PscStatementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final PscStatementRepository pscStatementRepository;
    private final PscStatementTransformer pscStatementTransformer;
    private final CompanyMetricsApiService companyMetricsApiService;
    private final CompanyExemptionsApiService companyExemptionsApiService;
    private final ChsKafkaApiService chsKafkaApiService;

    public PscStatementService(PscStatementRepository pscStatementRepository,
            PscStatementTransformer pscStatementTransformer, CompanyMetricsApiService companyMetricsApiService,
            CompanyExemptionsApiService companyExemptionsApiService, ChsKafkaApiService chsKafkaApiService) {
        this.pscStatementRepository = pscStatementRepository;
        this.pscStatementTransformer = pscStatementTransformer;
        this.companyMetricsApiService = companyMetricsApiService;
        this.companyExemptionsApiService = companyExemptionsApiService;
        this.chsKafkaApiService = chsKafkaApiService;
    }

    public Statement retrievePscStatementFromDb(String companyNumber, String statementId)
            throws ResourceNotFoundException {
        PscStatementDocument pscStatementDocument = getPscStatementDocument(companyNumber, statementId)
                .orElseThrow(() -> new ResourceNotFoundException(HttpStatusCode.valueOf(NOT_FOUND.value()),
                        "Record not found in MongoDB"));
        return pscStatementDocument.getData();
    }

    public StatementList retrievePscStatementListFromDb(String companyNumber, int startIndex, boolean registerView,
            int itemsPerPage) {

        Optional<MetricsApi> companyMetrics = companyMetricsApiService.getCompanyMetrics(companyNumber);

        if (registerView) {
            return retrievePscStatementListFromDbRegisterView(companyNumber, companyMetrics, startIndex, itemsPerPage);
        }
        List<PscStatementDocument> pscStatementDocuments = pscStatementRepository.getStatementList(
                companyNumber, startIndex, itemsPerPage);
        if (pscStatementDocuments.isEmpty()) {
            throw new ResourceNotFoundException(HttpStatusCode.valueOf(NOT_FOUND.value()), String.format(
                    "No PSC statements exists for company: %s", companyNumber));
        }
        return createStatementList(pscStatementDocuments, startIndex, itemsPerPage, companyMetrics, companyNumber,
                registerView);
    }

    public StatementList retrievePscStatementListFromDbRegisterView(String companyNumber,
            Optional<MetricsApi> companyMetrics, int startIndex, int itemsPerPage) {

        MetricsApi metricsData;
        metricsData = companyMetrics.orElseThrow(
                () -> new ResourceNotFoundException(HttpStatusCode.valueOf(NOT_FOUND.value()),
                        String.format("Metrics does not exist for company number: %s", companyNumber)));

        String registerMovedTo = Optional.ofNullable(metricsData)
                .map(MetricsApi::getRegisters)
                .map(RegistersApi::getPersonsWithSignificantControl)
                .map(RegisterApi::getRegisterMovedTo)
                .orElseThrow(() -> new ResourceNotFoundException(HttpStatusCode.valueOf(NOT_FOUND.value()),
                        String.format("Company %s is not on the public register", companyNumber)));

        if (registerMovedTo.equals("public-register")) {
            List<PscStatementDocument> pscStatementDocuments = pscStatementRepository.getStatementListRegisterView(
                    companyNumber, startIndex,
                    metricsData.getRegisters().getPersonsWithSignificantControl().getMovedOn(), itemsPerPage);
            if (pscStatementDocuments.isEmpty()) {
                throw new ResourceNotFoundException(HttpStatusCode.valueOf(NOT_FOUND.value()), String.format(
                        "No PSC statements exists for company: %s", companyNumber));
            }
            return createStatementList(pscStatementDocuments, startIndex, itemsPerPage, companyMetrics, companyNumber,
                    true);
        } else {
            throw new ResourceNotFoundException(HttpStatusCode.valueOf(NOT_FOUND.value()),
                    String.format("Company %s is not on the public register", companyNumber));
        }
    }

    public void deletePscStatement(String companyNumber, String statementId, String requestDeltaAt) {
        if (StringUtils.isBlank(requestDeltaAt)) {
            throw new BadRequestException("deltaAt missing from delete request");
        }
        try {
            Optional<PscStatementDocument> pscStatementDocument = getPscStatementDocument(companyNumber, statementId);

            pscStatementDocument.ifPresentOrElse(doc -> {
                String existingDeltaAt = doc.getDeltaAt();
                if (isDeltaStale(requestDeltaAt, existingDeltaAt)) {
                    throw new ConflictException(String.format("Stale delta received; request delta_at: [%s] "
                                    + "is not after existing delta_at: [%s]", requestDeltaAt, existingDeltaAt));
                }

                pscStatementRepository.delete(doc);
                chsKafkaApiService.invokeChsKafkaApiDelete(
                        new ResourceChangedRequest(companyNumber, statementId, doc, true));
            }, () -> {
                LOGGER.info("Delete for non-existent document", DataMapHolder.getLogMap());
                chsKafkaApiService.invokeChsKafkaApiDelete(
                        new ResourceChangedRequest(companyNumber, statementId, new PscStatementDocument(), true));
            });
        } catch (DataAccessException ex) {
            LOGGER.error("Error connecting to MongoDB", ex, DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("Error connecting to MongoDB");
        }
    }

    private Optional<PscStatementDocument> getPscStatementDocument(String companyNumber, String statementId) {
        return pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(companyNumber, statementId);
    }

    public void processPscStatement(String companyNumber, String statementId, CompanyPscStatement companyPscStatement) {
        String requestDeltaAt = companyPscStatement.getDeltaAt();
        if (StringUtils.isBlank(requestDeltaAt)) {
            LOGGER.error("deltaAt missing from request", DataMapHolder.getLogMap());
            throw new BadRequestException("deltaAt missing from request");
        }
        pscStatementRepository.getPscStatementByCompanyNumberAndStatementId(companyNumber, statementId)
                .ifPresentOrElse(
                        existingDoc -> {
                            String existingDeltaAt = existingDoc.getDeltaAt();
                            if (!isDeltaStale(requestDeltaAt, existingDeltaAt)) {
                                LOGGER.info("Updating existing document", DataMapHolder.getLogMap());
                                Created existingCreated = existingDoc.getCreated();
                                PscStatementDocument updatedDoc = pscStatementTransformer.transformPscStatement(
                                        companyNumber, statementId, companyPscStatement, existingCreated);
                                dbSaveUpsertApiCall(companyNumber, statementId, updatedDoc);
                            } else {
                                LOGGER.info("Psc Statement not persisted as the record provided is not the latest record.",
                                        DataMapHolder.getLogMap());
                                throw new ConflictException("Received stale delta");
                            }
                        },
                        () -> {
                            PscStatementDocument pscStatementDocument = pscStatementTransformer
                                    .transformPscStatement(companyNumber, statementId, companyPscStatement, null);
                            LOGGER.info("Inserting new document", DataMapHolder.getLogMap());
                            dbSaveUpsertApiCall(companyNumber, statementId, pscStatementDocument);
                        });
    }

    private void dbSaveUpsertApiCall(String companyNumber, String statementId, PscStatementDocument document) {
        try {
            pscStatementRepository.save(document);
        } catch (DataAccessException ex) {
            LOGGER.error("Error connecting to MongoDB", ex, DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("Error connecting to MongoDB");
        }
        chsKafkaApiService.invokeChsKafkaApi(new ResourceChangedRequest(
                companyNumber, statementId, null, false));
    }

    private StatementList createStatementList(List<PscStatementDocument> statementDocuments,
            int startIndex, int itemsPerPage, Optional<MetricsApi> companyMetrics, String companyNumber,
            boolean registerView) {

        StatementList statementList = new StatementList();
        StatementLinksType links = new StatementLinksType();
        links.setSelf(String.format("/company/%s/persons-with-significant-control-statements", companyNumber));
        if (hasActiveExemptions(companyNumber)) {
            links.setExemptions(String.format("/company/%s/exemptions", companyNumber));
        }

        List<Statement> statements = statementDocuments.stream().map(PscStatementDocument::getData).toList();

        companyMetrics.ifPresentOrElse(metricsApi -> {
            try {
                if (registerView) {
                    long withdrawnCount = statements.stream()
                            .filter(statement -> statement.getCeasedOn() != null).count();

                    statementList.setCeasedCount((int) withdrawnCount);
                    statementList.setTotalResults(
                            metricsApi.getCounts().getPersonsWithSignificantControl().getActiveStatementsCount()
                                    + statementList.getCeasedCount());
                    statementList.setActiveCount(
                            metricsApi.getCounts().getPersonsWithSignificantControl().getActiveStatementsCount());
                } else {
                    statementList.setActiveCount(
                            metricsApi.getCounts().getPersonsWithSignificantControl().getActiveStatementsCount());
                    statementList.setCeasedCount(
                            metricsApi.getCounts().getPersonsWithSignificantControl().getWithdrawnStatementsCount());
                    statementList.setTotalResults(
                            metricsApi.getCounts().getPersonsWithSignificantControl().getStatementsCount());
                }
            } catch (NullPointerException exp) {
                LOGGER.error(String.format("No PSC data in metrics for company number %s",
                        companyNumber), DataMapHolder.getLogMap());
            }
        }, () -> {
            LOGGER.info(String.format("Metrics does not exist for company number: %s",
                    companyNumber), DataMapHolder.getLogMap());
        });

        statementList.setItemsPerPage(itemsPerPage);
        statementList.setLinks(links);
        statementList.setStartIndex(startIndex);

        statementList.setItems(statements);
        return statementList;
    }

    private boolean hasActiveExemptions(String companyNumber) {
        Optional<CompanyExemptions> companyExemptions = companyExemptionsApiService.getCompanyExemptions(companyNumber);

        return companyExemptions.filter(x ->
                x.getExemptions() != null &&
                        ((x.getExemptions().getPscExemptAsSharesAdmittedOnMarket() != null &&
                                x.getExemptions().getPscExemptAsSharesAdmittedOnMarket().getItems().stream()
                                        .anyMatch(e -> e.getExemptTo() == null && e.getExemptFrom() != null)) ||
                                (x.getExemptions().getPscExemptAsTradingOnEuRegulatedMarket() != null &&
                                        x.getExemptions().getPscExemptAsTradingOnEuRegulatedMarket().getItems().stream()
                                                .anyMatch(e -> e.getExemptTo() == null && e.getExemptFrom() != null)) ||
                                (x.getExemptions().getPscExemptAsTradingOnRegulatedMarket() != null &&
                                        x.getExemptions().getPscExemptAsTradingOnRegulatedMarket().getItems().stream()
                                                .anyMatch(e -> e.getExemptTo() == null && e.getExemptFrom() != null)) ||
                                (x.getExemptions().getPscExemptAsTradingOnUkRegulatedMarket() != null &&
                                        x.getExemptions().getPscExemptAsTradingOnUkRegulatedMarket().getItems().stream()
                                                .anyMatch(e -> e.getExemptTo() == null && e.getExemptFrom() != null
                                                )))).isPresent();
    }

}
