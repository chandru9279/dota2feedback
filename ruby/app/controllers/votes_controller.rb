class VotesController < ApplicationController
  def create
    @vote = Vote.new(params[:vote])
    @vote.voteable = find_voteable
    @vote.ip_address = request.remote_ip
    if @vote.save
      respond_to do |format|
        format.js
      end
    else
      respond_to do |format|
        format.js { render :text => "alert('You already voted')" }
      end
    end
  end
  
  def find_voteable
    params.each do |name, value|
      if name =~ /(.+)_id$/
        return $1.classify.constantize.find(value)
      end
    end
    nil
  end
end