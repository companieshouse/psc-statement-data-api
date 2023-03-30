Feature: Process Psc Statement Requests

  Scenario Outline: Processing Psc Statement List GET request successfully

    Given Psc statements data api service is running
    And Psc statements exist for company number "<companyNumber>"
    When I send a GET statement list request for company number "<companyNumber>"
    Then I should receive 200 status code
    And the psc statement list Get call response body should match "<result>" file

    Examples:
      | companyNumber | result              |
      | OC421554      | psc_statements_list |


#  Scenario Outline: Processing Psc Statement List GET register view request successfully
#
#    Given Psc statements data api service is running
#    And Psc statements exist for company number "<companyNumber>"
#    When I send a GET statement list request for company number in register view "<companyNumber>"
#    Then I should receive 200 status code
#    And the psc statement list Get call response body should match "<result>" file
#
#    Examples:
#      | companyNumber | result              |
#      | OC421554      | psc_statements_list |