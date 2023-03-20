Feature: Process Psc Statement Requests

  Scenario Outline: Processing Psc Statement GET request successfully

    Given Psc statements data api service is running
    And a psc statement exists for company number "<companyNumber>" with statement id "<statementId>"
    When I send an GET request for company number "<companyNumber>" with statement id "<statementId>"
    Then I should receive 200 status code
    And the psc statement Get call response body should match "<result>" file

    Examples:
      | companyNumber | statementId                 | result        |
      | OC421554      | DHTUrJoAuKdXw7zvkreyAm_SoH0 | psc_statement |

