class Vote < ActiveRecord::Base
  belongs_to :voteable, :polymorphic => true
  
  validates :ip_address, :presence => true
  validates :option, :presence => true, :numericality => true
  validates :voteable, :presence => true
  
  validates_uniqueness_of :ip_address, :scope => [:voteable_type, :voteable_id]
  
  LIKE = 1
  DISLIKE = 0
  
  def after_save
    case option
    when LIKE
      voteable.increment!(:likes, 1)
    when DISLIKE
      voteable.increment!(:dislikes, 1)
    end
  end
end
