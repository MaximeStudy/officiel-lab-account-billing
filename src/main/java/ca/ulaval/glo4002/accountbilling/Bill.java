package ca.ulaval.glo4002.accountbilling;

import java.util.ArrayList;
import java.util.List;

public class Bill {

	private ClientId clientId;
	private List<Allocation> allocations = new ArrayList<>();
	private boolean cancelled = false;
	private int total;

	public Bill(ClientId clientId, int total) {
		this.clientId = clientId;
		this.total = total;
	}

	public ClientId getClientId() {
		return clientId;
	}

	public void cancel() {
		cancelled = true;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void addAllocation(Allocation allocation) {
		allocations.add(allocation);
	}

	public List<Allocation> getAllocations() {
		return allocations;
	}

	public int getRemainingAmount() {
		return total - allocations.stream().mapToInt(Allocation::getAmount).sum();
	}

	public int redistributeAllocation(Bill billToCancel, int amountToRedistribute) {
		if (this != billToCancel) {
			int remainingAmount = getRemainingAmount();
			Allocation newRedistributedAllocation;
			if (remainingAmount <= amountToRedistribute) {
				newRedistributedAllocation = new Allocation(remainingAmount);
				amountToRedistribute -= remainingAmount;
			} else {
				newRedistributedAllocation = new Allocation(amountToRedistribute);
				amountToRedistribute = 0;
			}

			addAllocation(newRedistributedAllocation);
		}

		return amountToRedistribute;
	}

	public void cancelBillIfNotCancelled() {
		if (!isCancelled()) {
			cancel();
		}
	}

}
