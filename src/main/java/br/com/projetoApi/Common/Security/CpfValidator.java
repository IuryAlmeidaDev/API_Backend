package br.com.projetoApi.Common.Security;

public final class CpfValidator {

    private CpfValidator() {
    }

    public static boolean isValid(String cpf) {
        if (cpf == null) {
            return false;
        }

        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() != 11 || digits.chars().distinct().count() == 1) {
            return false;
        }

        return calculateDigit(digits, 10) == Character.getNumericValue(digits.charAt(9))
                && calculateDigit(digits, 11) == Character.getNumericValue(digits.charAt(10));
    }

    private static int calculateDigit(String cpf, int weightStart) {
        int sum = 0;
        for (int i = 0; i < weightStart - 1; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (weightStart - i);
        }
        int result = 11 - (sum % 11);
        return result > 9 ? 0 : result;
    }
}
